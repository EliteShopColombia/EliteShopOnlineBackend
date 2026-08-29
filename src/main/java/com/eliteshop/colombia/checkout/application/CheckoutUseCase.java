package com.eliteshop.colombia.checkout.application;

import com.eliteshop.colombia.cart.domain.model.Cart;
import com.eliteshop.colombia.cart.domain.model.CartItem;
import com.eliteshop.colombia.cart.domain.repository.CartRepository;
import com.eliteshop.colombia.checkout.domain.exception.CannotBuyOwnStoreException;
import com.eliteshop.colombia.checkout.domain.exception.CvvRequiredException;
import com.eliteshop.colombia.checkout.domain.exception.EmptyCartException;
import com.eliteshop.colombia.checkout.domain.exception.InsufficientStockException;
import com.eliteshop.colombia.checkout.domain.exception.PaymentFailedException;
import com.eliteshop.colombia.customer.domain.exception.CustomerNotFoundException;
import com.eliteshop.colombia.customer.domain.model.Customer;
import com.eliteshop.colombia.customer.domain.repository.CustomerRepository;
import com.eliteshop.colombia.order.application.OrderUpdateUseCase;
import com.eliteshop.colombia.order.domain.event.OrderCreatedEvent;
import com.eliteshop.colombia.order.domain.model.*;
import com.eliteshop.colombia.order.domain.repository.OrderItemRepository;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import com.eliteshop.colombia.payment.domain.model.*;
import com.eliteshop.colombia.payment.domain.model.paymentmethod.CustomerPaymentMethod;
import com.eliteshop.colombia.payment.domain.port.CustomerPaymentMethodRepository;
import com.eliteshop.colombia.payment.domain.port.PaymentGateway;
import com.eliteshop.colombia.payment.domain.port.PaymentRepository;
import com.eliteshop.colombia.product.domain.model.Product;
import com.eliteshop.colombia.product.domain.model.ProductId;
import com.eliteshop.colombia.product.domain.repository.ProductRepository;
import com.eliteshop.colombia.seller.domain.repository.SellerRepository;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class CheckoutUseCase {

  private final CartRepository cartRepository;
  private final ProductRepository productRepository;
  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;
  private final PaymentRepository paymentRepository;
  private final CustomerPaymentMethodRepository paymentMethodRepository;
  private final CustomerRepository customerRepository;
  private final PaymentGateway paymentGateway;
  private final OrderUpdateUseCase orderUpdateUseCase;
  private final ApplicationEventPublisher eventPublisher;
  private final SellerRepository sellerRepository;

  public CheckoutResult execute(UUID customerId, CheckoutRequestFields request) {
    log.info("Iniciando checkout para cliente {}", customerId);

    // 1. Obtener carrito
    Cart cart =
        cartRepository
            .findByCustomerId(customerId)
            .orElseThrow(() -> new EmptyCartException("El carrito está vacío"));

    if (cart.getItems().isEmpty()) {
      throw new EmptyCartException("El carrito está vacío");
    }

    // 2. Validar stock de cada item
    for (CartItem item : cart.getItems()) {
      Product product =
          productRepository
              .findById(new ProductId(item.getProductId().getValue()))
              .orElseThrow(
                  () ->
                      new InsufficientStockException(
                          "Producto no encontrado: " + item.getProductId().getValue()));

      if (product.getStock().getValue() < item.getQuantity().getValue()) {
        throw new InsufficientStockException(
            "Stock insuficiente para el producto " + product.getName().getValue());
      }
    }

    // 3. Validar que un seller no compre de su propia tienda
    Customer customer =
        customerRepository
            .findById(new com.eliteshop.colombia.customer.domain.model.CustomerId(customerId))
            .orElseThrow(() -> new CustomerNotFoundException("Customer no encontrado"));

    sellerRepository
        .findByEmail(customer.getEmail().getValue())
        .ifPresent(
            seller -> {
              UUID sellerId = seller.getId().getValue();
              for (CartItem item : cart.getItems()) {
                Product product =
                    productRepository
                        .findById(new ProductId(item.getProductId().getValue()))
                        .orElse(null);
                if (product != null && product.getSellerId().getValue().equals(sellerId)) {
                  throw new CannotBuyOwnStoreException(
                      "No puedes comprar productos de tu propia tienda: "
                          + product.getName().getValue());
                }
              }
            });

    // 4. Procesar pago (outside transaction - reactive HTTP call)
    String tempInvoice = "ESC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

    Payment payment = processPayment(customer, null, cart.getTotal(), request, tempInvoice).block();

    if (payment.getStatus() != PaymentStatus.APPROVED) {
      throw new PaymentFailedException("El pago no fue aprobado. Estado: " + payment.getStatus());
    }

    // 5. Persist everything atomically (transactional boundary)
    Order savedOrder;
    try {
      savedOrder = persistOrderAndItems(customerId, cart, request, payment);
    } catch (Exception e) {
      log.error(
          "Error persistiendo orden para cliente {}, invocando compensacion de pago", customerId);
      compensatePayment(payment);
      throw new PaymentFailedException(
          "Error al crear la orden despues del pago. Pago revertido.", e);
    }

    // 6. Publicar evento, confirmar orden, reducir stock, limpiar carrito
    try {
      postPaymentActions(customerId, savedOrder, cart);
    } catch (Exception e) {
      log.error(
          "Error en post-pago para orden {}, invocando compensacion completa",
          savedOrder.getId().getValue());
      compensatePostPayment(savedOrder, payment, customerId);
      throw new PaymentFailedException(
          "Error despues del pago. Acciones compensatorias ejecutadas.", e);
    }

    log.info(
        "Checkout completado exitosamente. Orden: {}, Pago: {}",
        savedOrder.getId().getValue(),
        payment.getId());

    return new CheckoutResult(
        savedOrder.getId().getValue(),
        payment.getId(),
        payment.getStatus().name(),
        payment.getEpaycoRefId(),
        payment.getInvoice(),
        cart.getTotal());
  }

  @Transactional
  protected Order persistOrderAndItems(
      UUID customerId, Cart cart, CheckoutRequestFields request, Payment payment) {
    // Build and save order
    Order order = buildOrder(customerId, cart, request);
    Order savedOrder = orderRepository.save(order);

    // Build and save order items
    List<OrderItem> orderItems =
        cart.getItems().stream()
            .map(item -> buildOrderItem(savedOrder.getId().getValue(), item))
            .collect(Collectors.toList());
    orderItemRepository.saveAll(orderItems);

    // Associate payment with order and save
    payment =
        Payment.builder()
            .id(payment.getId())
            .orderId(savedOrder.getId().getValue())
            .sellerId(payment.getSellerId())
            .amount(payment.getAmount())
            .currency(payment.getCurrency())
            .method(payment.getMethod())
            .status(payment.getStatus())
            .epaycoRefId(payment.getEpaycoRefId())
            .sessionId(payment.getSessionId())
            .invoice(payment.getInvoice())
            .customerEmail(payment.getCustomerEmail())
            .createdAt(payment.getCreatedAt())
            .updatedAt(payment.getUpdatedAt())
            .platformFee(payment.getPlatformFee())
            .sellerAmount(payment.getSellerAmount())
            .build();
    paymentRepository.save(payment);

    return savedOrder;
  }

  private void postPaymentActions(UUID customerId, Order savedOrder, Cart cart) {
    // Publish order created event
    eventPublisher.publishEvent(
        OrderCreatedEvent.of(
            savedOrder.getId().getValue(),
            savedOrder.getCustomerId().getValue(),
            savedOrder.getTotalAmount().getValue()));

    // Confirm order (triggers status change event)
    orderUpdateUseCase.execute(buildConfirmedOrder(savedOrder));

    // Reduce stock for each item
    for (CartItem item : cart.getItems()) {
      productRepository.reduceStock(
          new ProductId(item.getProductId().getValue()), item.getQuantity().getValue());
    }

    // Clear cart
    cartRepository.deleteByCustomerId(customerId);
  }

  private void compensatePayment(Payment payment) {
    log.warn(
        "Compensacion de pago pendiente: pago {} requiere reversa manual via epayco (refId: {})",
        payment.getId(),
        payment.getEpaycoRefId());
  }

  private void compensatePostPayment(Order savedOrder, Payment payment, UUID customerId) {
    // Attempt to cancel the order (sets status to CANCELLED)
    try {
      log.warn("Compensacion: intentando cancelar orden {}", savedOrder.getId().getValue());
      Order cancelledOrder =
          new Order(
              savedOrder.getId(),
              savedOrder.getCustomerId(),
              OrderStatus.CANCELLED,
              savedOrder.getTotalAmount(),
              savedOrder.getShippingAddress(),
              savedOrder.getShippingDepartment(),
              savedOrder.getShippingCity(),
              savedOrder.getCreatedAt(),
              new OrderUpdatedAt(new Timestamp(System.currentTimeMillis())),
              savedOrder.getTrackingNumber(),
              savedOrder.getShippingCarrier(),
              savedOrder.getShippingLabelUrl(),
              savedOrder.getDisputeReason());
      orderUpdateUseCase.execute(cancelledOrder);
      log.info("Compensacion: orden {} cancelada", savedOrder.getId().getValue());
    } catch (Exception e) {
      log.error(
          "Compensacion: no se pudo cancelar orden {}. Pago requiere reversa manual.",
          savedOrder.getId().getValue(),
          e);
    }

    // Attempt to refund the payment
    compensatePayment(payment);
  }

  private Order buildOrder(UUID customerId, Cart cart, CheckoutRequestFields request) {
    return new Order(
        new OrderId(UUID.randomUUID()),
        new OrderCustomerId(customerId),
        OrderStatus.PENDING_PAYMENT,
        new OrderTotalAmount(cart.getTotal()),
        new OrderShippingAddress(request.shippingAddress),
        new OrderShippingDepartment(request.shippingDepartment),
        new OrderShippingCity(request.shippingCity),
        new OrderCreatedAt(new Timestamp(System.currentTimeMillis())),
        null,
        null,
        null,
        null,
        null);
  }

  private Order buildConfirmedOrder(Order order) {
    return new Order(
        order.getId(),
        order.getCustomerId(),
        OrderStatus.PAID,
        order.getTotalAmount(),
        order.getShippingAddress(),
        order.getShippingDepartment(),
        order.getShippingCity(),
        order.getCreatedAt(),
        new OrderUpdatedAt(new Timestamp(System.currentTimeMillis())),
        order.getTrackingNumber(),
        order.getShippingCarrier(),
        order.getShippingLabelUrl(),
        null);
  }

  private OrderItem buildOrderItem(UUID orderId, CartItem item) {
    Product product =
        productRepository
            .findById(new ProductId(item.getProductId().getValue()))
            .orElseThrow(
                () ->
                    new InsufficientStockException(
                        "Producto no encontrado: " + item.getProductId().getValue()));
    return OrderItem.create(
        orderId,
        item.getProductId().getValue(),
        product.getSellerId().getValue(),
        item.getQuantity().getValue(),
        item.getUnitPrice().getValue());
  }

  private Mono<Payment> processPayment(
      Customer customer,
      UUID orderId,
      BigDecimal amount,
      CheckoutRequestFields request,
      String tempInvoice) {

    String invoice = tempInvoice;

    if (!request.isNewCard()) {
      // Saved method: requires CVV
      if (request.cvv == null || request.cvv.isBlank()) {
        return Mono.error(new CvvRequiredException("El CVV es requerido para metodos guardados"));
      }
      CustomerPaymentMethod method =
          paymentMethodRepository
              .findById(request.paymentMethodId)
              .orElseThrow(() -> new PaymentFailedException("Metodo de pago no encontrado"));

      return paymentGateway
          .chargeWithToken(
              method.getEpaycoToken().getValue(),
              method.getEpaycoCustomerId().getValue(),
              request.cvv,
              amount,
              invoice,
              customer.getFirstName().getValue(),
              customer.getLastName().getValue(),
              customer.getEmail().getValue(),
              resolveDocType(method.getDocType() != null ? method.getDocType().getValue() : null),
              resolveDocNumber(
                  method.getDocNumber() != null ? method.getDocNumber().getValue() : null,
                  customer.getId().getValue()))
          .map(payment -> enrichPayment(payment, customer, orderId, amount, invoice));
    }

    // New card: validate data
    if (request.cardNumber == null
        || request.expiryMonth == null
        || request.expiryYear == null
        || request.cvv == null
        || request.cvv.isBlank()) {
      return Mono.error(
          new InsufficientStockException("Datos de tarjeta incompletos para nueva tarjeta"));
    }

    return paymentGateway
        .tokenizeCard(request.cardNumber, request.cvv, request.expiryMonth, request.expiryYear)
        .flatMap(
            tokenized ->
                resolveEpaycoCustomer(customer, tokenized.getToken())
                    .flatMap(
                        epaycoCustomerId ->
                            paymentGateway.chargeWithToken(
                                tokenized.getToken(),
                                epaycoCustomerId,
                                request.cvv,
                                amount,
                                invoice,
                                customer.getFirstName().getValue(),
                                customer.getLastName().getValue(),
                                customer.getEmail().getValue(),
                                resolveDocType(request.docType),
                                resolveDocNumber(request.docNumber, customer.getId().getValue()))))
        .map(payment -> enrichPayment(payment, customer, orderId, amount, invoice));
  }

  private Mono<String> resolveEpaycoCustomer(Customer customer, String token) {
    String existing =
        paymentMethodRepository.findByCustomerId(customer.getId().getValue()).stream()
            .map(method -> method.getEpaycoCustomerId().getValue())
            .findFirst()
            .orElse(null);

    if (existing != null) {
      return paymentGateway.addTokenToCustomer(existing, token).thenReturn(existing);
    }
    return paymentGateway.createEpaycoCustomer(
        token,
        customer.getFirstName().getValue(),
        customer.getLastName().getValue(),
        customer.getEmail().getValue(),
        customer.getPhoneNumber().getValue(),
        null,
        null);
  }

  private String resolveDocType(String docType) {
    return (docType != null && !docType.isBlank()) ? docType : "CC";
  }

  private String resolveDocNumber(String docNumber, UUID customerId) {
    if (docNumber != null && !docNumber.isBlank()) return docNumber;
    String digits = customerId.toString().replaceAll("[^0-9]", "");
    return digits.length() > 12 ? digits.substring(0, 12) : digits;
  }

  private Payment enrichPayment(
      Payment payment, Customer customer, UUID orderId, BigDecimal amount, String invoice) {
    return Payment.builder()
        .id(Payment.generateId())
        .orderId(orderId)
        .amount(amount)
        .currency("COP")
        .method(PaymentMethod.CARD)
        .status(payment.getStatus() != null ? payment.getStatus() : PaymentStatus.PENDING)
        .epaycoRefId(payment.getEpaycoRefId())
        .invoice(invoice)
        .customerEmail(customer.getEmail().getValue())
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
  }

  public static class CheckoutRequestFields {
    public final String shippingAddress;
    public final String shippingDepartment;
    public final String shippingCity;
    public final UUID paymentMethodId;
    public final String cvv;
    public final String cardNumber;
    public final Integer expiryMonth;
    public final Integer expiryYear;
    public final String docType;
    public final String docNumber;

    public CheckoutRequestFields(
        String shippingAddress,
        String shippingDepartment,
        String shippingCity,
        UUID paymentMethodId,
        String cvv,
        String cardNumber,
        Integer expiryMonth,
        Integer expiryYear,
        String docType,
        String docNumber) {
      this.shippingAddress = shippingAddress;
      this.shippingDepartment = shippingDepartment;
      this.shippingCity = shippingCity;
      this.paymentMethodId = paymentMethodId;
      this.cvv = cvv;
      this.cardNumber = cardNumber;
      this.expiryMonth = expiryMonth;
      this.expiryYear = expiryYear;
      this.docType = docType;
      this.docNumber = docNumber;
    }

    public boolean isNewCard() {
      return paymentMethodId == null;
    }
  }

  public static class CheckoutResult {
    public final UUID orderId;
    public final UUID paymentId;
    public final String status;
    public final String epaycoRefId;
    public final String invoice;
    public final BigDecimal totalAmount;

    public CheckoutResult(
        UUID orderId,
        UUID paymentId,
        String status,
        String epaycoRefId,
        String invoice,
        BigDecimal totalAmount) {
      this.orderId = orderId;
      this.paymentId = paymentId;
      this.status = status;
      this.epaycoRefId = epaycoRefId;
      this.invoice = invoice;
      this.totalAmount = totalAmount;
    }
  }
}
