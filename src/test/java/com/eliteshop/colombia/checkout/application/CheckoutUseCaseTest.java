package com.eliteshop.colombia.checkout.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.eliteshop.colombia.cart.domain.model.*;
import com.eliteshop.colombia.cart.domain.repository.CartRepository;
import com.eliteshop.colombia.checkout.domain.exception.*;
import com.eliteshop.colombia.customer.domain.model.*;
import com.eliteshop.colombia.customer.domain.repository.CustomerRepository;
import com.eliteshop.colombia.order.application.OrderUpdateUseCase;
import com.eliteshop.colombia.order.domain.model.*;
import com.eliteshop.colombia.order.domain.repository.OrderItemRepository;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import com.eliteshop.colombia.payment.domain.model.*;
import com.eliteshop.colombia.payment.domain.model.paymentmethod.TokenizedCard;
import com.eliteshop.colombia.payment.domain.port.CustomerPaymentMethodRepository;
import com.eliteshop.colombia.payment.domain.port.PaymentGateway;
import com.eliteshop.colombia.payment.domain.port.PaymentRepository;
import com.eliteshop.colombia.product.domain.model.*;
import com.eliteshop.colombia.product.domain.repository.ProductRepository;
import com.eliteshop.colombia.seller.domain.model.Seller;
import com.eliteshop.colombia.seller.domain.model.SellerId;
import com.eliteshop.colombia.seller.domain.repository.SellerRepository;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class CheckoutUseCaseTest {

  @Mock private CartRepository cartRepository;
  @Mock private ProductRepository productRepository;
  @Mock private OrderRepository orderRepository;
  @Mock private OrderItemRepository orderItemRepository;
  @Mock private PaymentRepository paymentRepository;
  @Mock private CustomerPaymentMethodRepository paymentMethodRepository;
  @Mock private CustomerRepository customerRepository;
  @Mock private PaymentGateway paymentGateway;
  @Mock private OrderUpdateUseCase orderUpdateUseCase;
  @Mock private ApplicationEventPublisher eventPublisher;
  @Mock private SellerRepository sellerRepository;

  @Mock
  private com.eliteshop.colombia.shared.domain.LocationValidationService locationValidationService;

  private CheckoutUseCase checkoutUseCase;

  private final UUID customerId = UUID.randomUUID();
  private final UUID productId = UUID.randomUUID();
  private final UUID sellerId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    checkoutUseCase =
        new CheckoutUseCase(
            cartRepository,
            productRepository,
            orderRepository,
            orderItemRepository,
            paymentRepository,
            paymentMethodRepository,
            customerRepository,
            paymentGateway,
            orderUpdateUseCase,
            eventPublisher,
            sellerRepository,
            locationValidationService);
  }

  @Test
  void shouldThrowWhenCartIsEmpty() {
    when(cartRepository.findByCustomerId(customerId)).thenReturn(Optional.empty());

    CheckoutUseCase.CheckoutRequestFields fields = buildRequestFields();

    assertThatThrownBy(() -> checkoutUseCase.execute(customerId, fields))
        .isInstanceOf(EmptyCartException.class)
        .hasMessageContaining("vacío");
  }

  @Test
  void shouldThrowWhenCartHasNoItems() {
    Cart cart = Cart.create(customerId);
    when(cartRepository.findByCustomerId(customerId)).thenReturn(Optional.of(cart));

    CheckoutUseCase.CheckoutRequestFields fields = buildRequestFields();

    assertThatThrownBy(() -> checkoutUseCase.execute(customerId, fields))
        .isInstanceOf(EmptyCartException.class);
  }

  @Test
  void shouldThrowWhenStockIsInsufficient() {
    Cart cart = buildCartWithOneItem(5);
    when(cartRepository.findByCustomerId(customerId)).thenReturn(Optional.of(cart));

    Product product = buildProduct(3);
    when(productRepository.findById(any())).thenReturn(Optional.of(product));

    CheckoutUseCase.CheckoutRequestFields fields = buildRequestFields();

    assertThatThrownBy(() -> checkoutUseCase.execute(customerId, fields))
        .isInstanceOf(InsufficientStockException.class)
        .hasMessageContaining("Stock insuficiente");

    verifyNoInteractions(paymentGateway);
  }

  @Test
  void shouldThrowWhenSellerBuysOwnProduct() {
    Cart cart = buildCartWithOneItem(1);
    when(cartRepository.findByCustomerId(customerId)).thenReturn(Optional.of(cart));

    Product product = buildProduct(10);
    when(productRepository.findById(any())).thenReturn(Optional.of(product));
    when(customerRepository.findById(any())).thenReturn(Optional.of(buildCustomer()));

    Seller seller = mock(Seller.class);
    when(seller.getId()).thenReturn(new SellerId(sellerId));
    when(sellerRepository.findByEmail(any())).thenReturn(Optional.of(seller));

    CheckoutUseCase.CheckoutRequestFields fields = buildRequestFields();

    assertThatThrownBy(() -> checkoutUseCase.execute(customerId, fields))
        .isInstanceOf(CannotBuyOwnStoreException.class)
        .hasMessageContaining("propia tienda");
  }

  @Test
  void shouldThrowWhenPaymentIsNotApproved() {
    Cart cart = buildCartWithOneItem(1);
    when(cartRepository.findByCustomerId(customerId)).thenReturn(Optional.of(cart));

    Product product = buildProduct(10);
    when(productRepository.findById(any())).thenReturn(Optional.of(product));
    when(customerRepository.findById(any())).thenReturn(Optional.of(buildCustomer()));
    when(sellerRepository.findByEmail(any())).thenReturn(Optional.empty());

    Payment failedPayment =
        Payment.builder().status(PaymentStatus.DECLINED).epaycoRefId("ref-fail").build();
    when(paymentGateway.tokenizeCard(any(), any(), anyInt(), anyInt()))
        .thenReturn(Mono.just(new TokenizedCard("tok", "1111", "VISA", 12, 2028)));
    when(paymentGateway.createEpaycoCustomer(any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(Mono.just("cus-123"));
    when(paymentGateway.chargeWithToken(
            any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(Mono.just(failedPayment));

    CheckoutUseCase.CheckoutRequestFields fields = buildNewCardRequestFields();

    assertThatThrownBy(() -> checkoutUseCase.execute(customerId, fields))
        .isInstanceOf(PaymentFailedException.class)
        .hasMessageContaining("no fue aprobado");
  }

  @Test
  void shouldReduceStockAndClearCartWhenCheckoutSucceeds() {
    Cart cart = buildCartWithOneItem(2);
    when(cartRepository.findByCustomerId(customerId)).thenReturn(Optional.of(cart));

    Product product = buildProduct(10);
    when(productRepository.findById(any())).thenReturn(Optional.of(product));
    when(customerRepository.findById(any())).thenReturn(Optional.of(buildCustomer()));
    when(sellerRepository.findByEmail(any())).thenReturn(Optional.empty());

    Payment approvedPayment =
        Payment.builder()
            .id(UUID.randomUUID())
            .status(PaymentStatus.APPROVED)
            .epaycoRefId("ref-123")
            .invoice("INV-123")
            .build();
    when(paymentGateway.tokenizeCard(any(), any(), anyInt(), anyInt()))
        .thenReturn(Mono.just(new TokenizedCard("tok", "1111", "VISA", 12, 2028)));
    when(paymentGateway.createEpaycoCustomer(any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(Mono.just("cus-123"));
    when(paymentGateway.chargeWithToken(
            any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(Mono.just(approvedPayment));

    Order savedOrder =
        new Order(
            new OrderId(UUID.randomUUID()),
            new OrderCustomerId(customerId),
            OrderStatus.PENDING_PAYMENT,
            new OrderTotalAmount(cart.getTotal()),
            new OrderShippingAddress("Calle 123"),
            new OrderShippingDepartment("Bogota"),
            new OrderShippingCity("Bogota"),
            new OrderCreatedAt(new Timestamp(System.currentTimeMillis())),
            null,
            null,
            null,
            null,
            null);
    when(orderRepository.save(any())).thenReturn(savedOrder);
    when(orderItemRepository.saveAll(any())).thenReturn(List.of());

    CheckoutUseCase.CheckoutRequestFields fields = buildNewCardRequestFields();
    CheckoutUseCase.CheckoutResult result = checkoutUseCase.execute(customerId, fields);

    assertThat(result).isNotNull();
    assertThat(result.status).isEqualTo("APPROVED");
    verify(productRepository).reduceStock(any(), eq(2));
    verify(cartRepository).deleteByCustomerId(customerId);
  }

  private CheckoutUseCase.CheckoutRequestFields buildRequestFields() {
    return new CheckoutUseCase.CheckoutRequestFields(
        "Calle 123", "Bogota", "Bogota", null, null, null, null, null, null, null);
  }

  private CheckoutUseCase.CheckoutRequestFields buildNewCardRequestFields() {
    return new CheckoutUseCase.CheckoutRequestFields(
        "Calle 123",
        "Bogota",
        "Bogota",
        null,
        "123",
        "4111111111111111",
        12,
        2028,
        "CC",
        "1234567890");
  }

  private Cart buildCartWithOneItem(int quantity) {
    Cart cart = Cart.create(customerId);
    CartItemProductId cartProductId = new CartItemProductId(productId);
    CartItemQuantity cartQuantity = new CartItemQuantity(quantity);
    CartItemUnitPrice cartPrice = new CartItemUnitPrice(new BigDecimal("50000"));
    cart.addItem(cartProductId, cartQuantity, cartPrice);
    return cart;
  }

  private Product buildProduct(int stock) {
    return new Product(
        new ProductId(productId),
        new ProductSellerId(sellerId),
        new ProductName("Laptop"),
        new ProductPrice(new BigDecimal("50000")),
        new ProductStock(stock),
        List.of());
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
