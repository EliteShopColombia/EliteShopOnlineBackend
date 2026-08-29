package com.eliteshop.colombia.order.infrastructure.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eliteshop.colombia.order.application.AddTrackingEventUseCase;
import com.eliteshop.colombia.order.application.CancelOrderUseCase;
import com.eliteshop.colombia.order.application.CompleteOrderUseCase;
import com.eliteshop.colombia.order.application.ConfirmDeliveryUseCase;
import com.eliteshop.colombia.order.application.DisputeOrderUseCase;
import com.eliteshop.colombia.order.application.FindOrdersByCustomerIdUseCase;
import com.eliteshop.colombia.order.application.FindOrdersBySellerUseCase;
import com.eliteshop.colombia.order.application.GetTrackingEventsUseCase;
import com.eliteshop.colombia.order.application.OrderDeleteUseCase;
import com.eliteshop.colombia.order.application.OrderFindByIdUseCase;
import com.eliteshop.colombia.order.application.OrderSaveUseCase;
import com.eliteshop.colombia.order.application.OrderStatusCountsUseCase;
import com.eliteshop.colombia.order.application.OrderSummaryUseCase;
import com.eliteshop.colombia.order.application.OrderUpdateUseCase;
import com.eliteshop.colombia.order.application.OutForDeliveryUseCase;
import com.eliteshop.colombia.order.application.PrepareOrderUseCase;
import com.eliteshop.colombia.order.application.RefundOrderUseCase;
import com.eliteshop.colombia.order.application.SearchOrdersUseCase;
import com.eliteshop.colombia.order.application.ShipOrderUseCase;
import com.eliteshop.colombia.order.application.UpdateTrackingUseCase;
import com.eliteshop.colombia.order.domain.model.Order;
import com.eliteshop.colombia.order.domain.model.OrderCreatedAt;
import com.eliteshop.colombia.order.domain.model.OrderCustomerId;
import com.eliteshop.colombia.order.domain.model.OrderId;
import com.eliteshop.colombia.order.domain.model.OrderShippingAddress;
import com.eliteshop.colombia.order.domain.model.OrderShippingCity;
import com.eliteshop.colombia.order.domain.model.OrderShippingDepartment;
import com.eliteshop.colombia.order.domain.model.OrderStatus;
import com.eliteshop.colombia.order.domain.model.OrderTotalAmount;
import com.eliteshop.colombia.order.domain.repository.OrderItemRepository;
import com.eliteshop.colombia.order.infrastructure.mapper.OrderMapper;
import com.eliteshop.colombia.shared.exception.GlobalExceptionHandler;
import com.eliteshop.colombia.shared.security.AuthorizationService;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(SpringExtension.class)
class OrderControllerAuthorizationTest {

  private final OrderFindByIdUseCase findByIdUseCase =
      org.mockito.Mockito.mock(OrderFindByIdUseCase.class);
  private final OrderMapper mapper = org.mockito.Mockito.mock(OrderMapper.class);
  private final OrderItemRepository orderItemRepository =
      org.mockito.Mockito.mock(OrderItemRepository.class);
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    OrderAuthorizationHelper authHelper =
        new OrderAuthorizationHelper(
            findByIdUseCase, orderItemRepository, new AuthorizationService());

    OrderManagementController managementController =
        new OrderManagementController(
            org.mockito.Mockito.mock(OrderSaveUseCase.class),
            org.mockito.Mockito.mock(OrderUpdateUseCase.class),
            org.mockito.Mockito.mock(OrderDeleteUseCase.class),
            findByIdUseCase,
            org.mockito.Mockito.mock(FindOrdersByCustomerIdUseCase.class),
            mapper,
            authHelper);

    OrderStatusController statusController =
        new OrderStatusController(
            org.mockito.Mockito.mock(CancelOrderUseCase.class),
            org.mockito.Mockito.mock(ConfirmDeliveryUseCase.class),
            org.mockito.Mockito.mock(PrepareOrderUseCase.class),
            org.mockito.Mockito.mock(ShipOrderUseCase.class),
            org.mockito.Mockito.mock(OutForDeliveryUseCase.class),
            org.mockito.Mockito.mock(CompleteOrderUseCase.class),
            org.mockito.Mockito.mock(DisputeOrderUseCase.class),
            org.mockito.Mockito.mock(RefundOrderUseCase.class),
            org.mockito.Mockito.mock(UpdateTrackingUseCase.class),
            org.mockito.Mockito.mock(AddTrackingEventUseCase.class),
            org.mockito.Mockito.mock(GetTrackingEventsUseCase.class),
            authHelper);

    OrderSellerController sellerController =
        new OrderSellerController(
            org.mockito.Mockito.mock(FindOrdersBySellerUseCase.class),
            org.mockito.Mockito.mock(OrderSummaryUseCase.class),
            org.mockito.Mockito.mock(SearchOrdersUseCase.class),
            org.mockito.Mockito.mock(OrderStatusCountsUseCase.class),
            mapper,
            authHelper);

    mockMvc =
        MockMvcBuilders.standaloneSetup(managementController, statusController, sellerController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Test
  void shouldAllowWhenCustomerReadsOwnOrder() throws Exception {
    UUID customerId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    Order order = order(orderId, customerId);
    when(findByIdUseCase.execute(any(OrderId.class))).thenReturn(Optional.of(order));
    when(mapper.toResponseWithItems(order)).thenReturn(null);

    mockMvc
        .perform(get("/api/v1/orders/{id}", orderId).principal(authentication(customerId)))
        .andExpect(status().isOk());
  }

  @Test
  void shouldRejectWhenReadingAnotherOrder() throws Exception {
    UUID ownerId = UUID.randomUUID();
    UUID authenticatedCustomerId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    when(findByIdUseCase.execute(any(OrderId.class)))
        .thenReturn(Optional.of(order(orderId, ownerId)));

    mockMvc
        .perform(
            get("/api/v1/orders/{id}", orderId).principal(authentication(authenticatedCustomerId)))
        .andExpect(status().isForbidden());
  }

  private UsernamePasswordAuthenticationToken authentication(UUID customerId) {
    return new UsernamePasswordAuthenticationToken(
        customerId.toString(),
        null,
        java.util.List.of(
            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                "ROLE_CUSTOMER")));
  }

  private Order order(UUID orderId, UUID customerId) {
    return new Order(
        new OrderId(orderId),
        new OrderCustomerId(customerId),
        OrderStatus.PAID,
        new OrderTotalAmount(new BigDecimal("100000")),
        new OrderShippingAddress("Calle 1"),
        new OrderShippingDepartment("Bogota"),
        new OrderShippingCity("Bogota"),
        new OrderCreatedAt(Timestamp.from(Instant.now())),
        null,
        null,
        null,
        null,
        null);
  }
}
