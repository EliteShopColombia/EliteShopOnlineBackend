package com.eliteshop.colombia.payment.infrastructure.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eliteshop.colombia.customer.domain.model.*;
import com.eliteshop.colombia.customer.domain.model.CustomerCreatedAt;
import com.eliteshop.colombia.customer.domain.model.CustomerPassword;
import com.eliteshop.colombia.customer.domain.model.CustomerPhoneNumber;
import com.eliteshop.colombia.customer.domain.model.CustomerRole;
import com.eliteshop.colombia.customer.domain.repository.CustomerRepository;
import com.eliteshop.colombia.order.domain.model.*;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import com.eliteshop.colombia.payment.application.usecase.*;
import com.eliteshop.colombia.payment.domain.model.*;
import com.eliteshop.colombia.payment.infrastructure.dto.CreateCheckoutSessionRequest;
import com.eliteshop.colombia.shared.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class PaymentControllerTest {

  private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private CreateCheckoutSessionUseCase createCheckoutSessionUseCase;
  private ConfirmPaymentUseCase confirmPaymentUseCase;
  private RetryPaymentUseCase retryPaymentUseCase;
  private RetryWithSavedCardUseCase retryWithSavedCardUseCase;
  private OrderRepository orderRepository;
  private CustomerRepository customerRepository;

  private final UUID customerId = UUID.randomUUID();
  private final UUID orderId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    createCheckoutSessionUseCase = mock(CreateCheckoutSessionUseCase.class);
    confirmPaymentUseCase = mock(ConfirmPaymentUseCase.class);
    retryPaymentUseCase = mock(RetryPaymentUseCase.class);
    retryWithSavedCardUseCase = mock(RetryWithSavedCardUseCase.class);
    orderRepository = mock(OrderRepository.class);
    customerRepository = mock(CustomerRepository.class);

    PaymentController controller =
        new PaymentController(
            createCheckoutSessionUseCase,
            confirmPaymentUseCase,
            retryPaymentUseCase,
            retryWithSavedCardUseCase,
            orderRepository,
            customerRepository);

    mockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Test
  void shouldReturnSessionIdWhenCreatingCheckoutSession() throws Exception {
    Order order = buildOrder(customerId);
    when(orderRepository.findById(any())).thenReturn(Optional.of(order));
    when(customerRepository.findById(any())).thenReturn(Optional.of(buildCustomer()));

    CheckoutSession session =
        CheckoutSession.builder().sessionId("sess-abc").token("tok-xyz").invoice("INV-123").build();
    when(createCheckoutSessionUseCase.execute(any()))
        .thenReturn(reactor.core.publisher.Mono.just(session));

    CreateCheckoutSessionRequest req = new CreateCheckoutSessionRequest();
    req.setOrderId(orderId);
    req.setAmount(new java.math.BigDecimal("150000"));
    req.setPaymentMethod("CARD");

    mockMvc
        .perform(
            post("/api/v1/payments/checkout-session")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .requestAttr("gateway.userId", customerId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.sessionId").value("sess-abc"))
        .andExpect(jsonPath("$.token").value("tok-xyz"))
        .andExpect(jsonPath("$.invoice").value("INV-123"));
  }

  @Test
  void shouldReturn400WhenCustomerIsWrong() throws Exception {
    Order order = buildOrder(UUID.randomUUID());
    when(orderRepository.findById(any())).thenReturn(Optional.of(order));

    CreateCheckoutSessionRequest req = new CreateCheckoutSessionRequest();
    req.setOrderId(orderId);
    req.setAmount(new java.math.BigDecimal("150000"));

    mockMvc
        .perform(
            post("/api/v1/payments/checkout-session")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .requestAttr("gateway.userId", customerId.toString()))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn200WhenConfirmingPayment() throws Exception {
    Payment payment =
        Payment.builder()
            .id(UUID.randomUUID())
            .orderId(orderId)
            .status(PaymentStatus.APPROVED)
            .epaycoRefId("ref-123")
            .invoice("INV-456")
            .build();
    when(confirmPaymentUseCase.execute("ref-123"))
        .thenReturn(reactor.core.publisher.Mono.just(payment));

    Order order = buildOrder(customerId);
    when(orderRepository.findById(any())).thenReturn(Optional.of(order));

    mockMvc
        .perform(
            post("/api/v1/payments/confirm/ref-123")
                .requestAttr("gateway.userId", customerId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("APPROVED"))
        .andExpect(jsonPath("$.refId").value("ref-123"));
  }

  @Test
  void shouldReturn200WhenGettingPaymentByInvoice() throws Exception {
    Payment payment =
        Payment.builder()
            .id(UUID.randomUUID())
            .orderId(orderId)
            .status(PaymentStatus.PENDING)
            .invoice("INV-789")
            .build();
    when(confirmPaymentUseCase.getPaymentByInvoice("INV-789"))
        .thenReturn(reactor.core.publisher.Mono.just(payment));

    Order order = buildOrder(customerId);
    when(orderRepository.findById(any())).thenReturn(Optional.of(order));

    mockMvc
        .perform(
            get("/api/v1/payments/INV-789").requestAttr("gateway.userId", customerId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("PENDING"))
        .andExpect(jsonPath("$.invoice").value("INV-789"));
  }

  @Test
  void shouldReturnSessionWhenRetryingWithSmartCheckout() throws Exception {
    Order order = buildOrder(customerId);
    when(orderRepository.findById(any())).thenReturn(Optional.of(order));

    CheckoutSession session =
        CheckoutSession.builder()
            .sessionId("sess-retry")
            .token("tok-retry")
            .invoice("INV-RETRY")
            .build();
    when(retryPaymentUseCase.execute(orderId))
        .thenReturn(reactor.core.publisher.Mono.just(session));

    mockMvc
        .perform(
            post("/api/v1/payments/orders/" + orderId + "/retry")
                .contentType(MediaType.APPLICATION_JSON)
                .requestAttr("gateway.userId", customerId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.sessionId").value("sess-retry"));
  }

  @Test
  void shouldReturn400WhenRetryingForOtherCustomer() throws Exception {
    Order order = buildOrder(UUID.randomUUID());
    when(orderRepository.findById(any())).thenReturn(Optional.of(order));

    mockMvc
        .perform(
            post("/api/v1/payments/orders/" + orderId + "/retry")
                .contentType(MediaType.APPLICATION_JSON)
                .requestAttr("gateway.userId", customerId.toString()))
        .andExpect(status().isBadRequest());
  }

  private Order buildUUID(UUID customerUuid) {
    return buildOrder(customerUuid);
  }

  private Order buildOrder(UUID custId) {
    return new Order(
        new OrderId(orderId),
        new OrderCustomerId(custId),
        OrderStatus.PAID,
        new OrderTotalAmount(new BigDecimal("150000")),
        new OrderShippingAddress("Calle 456"),
        new OrderShippingDepartment("Medellin"),
        new OrderShippingCity("Medellin"),
        new OrderCreatedAt(new Timestamp(System.currentTimeMillis())),
        null,
        null,
        null,
        null,
        null);
  }

  private Customer buildCustomer() {
    return new Customer(
        new CustomerId(customerId),
        new CustomerFirstName("Juan"),
        new CustomerLastName("Perez"),
        new CustomerEmail("juan@test.com"),
        new CustomerPhoneNumber("3001234567"),
        new CustomerPassword("hashed"),
        null,
        new CustomerRole("customer"),
        new CustomerCreatedAt(new java.sql.Timestamp(System.currentTimeMillis())),
        null,
        null);
  }
}
