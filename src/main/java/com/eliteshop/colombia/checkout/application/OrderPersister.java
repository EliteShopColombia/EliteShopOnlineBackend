package com.eliteshop.colombia.checkout.application;

import com.eliteshop.colombia.cart.domain.model.Cart;
import com.eliteshop.colombia.checkout.domain.exception.InsufficientStockException;
import com.eliteshop.colombia.order.domain.model.*;
import com.eliteshop.colombia.order.domain.repository.OrderItemRepository;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import com.eliteshop.colombia.payment.domain.model.Payment;
import com.eliteshop.colombia.payment.domain.port.PaymentRepository;
import com.eliteshop.colombia.product.domain.model.ProductId;
import com.eliteshop.colombia.product.domain.repository.ProductRepository;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistencia transaccional de la orden, sus items y la asociación del pago.
 *
 * <p>Vive en un bean propio a propósito: cuando el método anotado con {@code @Transactional} estaba
 * en {@code CheckoutUseCase} y se invocaba desde otro método del mismo bean, Spring se saltaba el
 * proxy y la transacción nunca se abría, de modo que un fallo al guardar los items dejaba la orden
 * persistida sin ellos y sin pago asociado.
 */
@Slf4j
@RequiredArgsConstructor
public class OrderPersister {

  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;
  private final ProductRepository productRepository;
  private final PaymentRepository paymentRepository;

  @Transactional
  public Order persistOrderAndItems(
      UUID customerId,
      Cart cart,
      CheckoutUseCase.CheckoutRequestFields request,
      Payment payment) {
    Order order = buildOrder(customerId, cart, request);
    Order savedOrder = orderRepository.save(order);

    List<OrderItem> orderItems =
        cart.getItems().stream()
            .map(item -> buildOrderItem(savedOrder.getId().getValue(), item))
            .toList();
    orderItemRepository.saveAll(orderItems);

    // Asociar el pago a la orden recién creada.
    Payment associatedPayment =
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
    paymentRepository.save(associatedPayment);

    log.info(
        "Orden persistida id={} con {} items y pago {} asociado",
        savedOrder.getId().getValue(),
        orderItems.size(),
        payment.getId());
    return savedOrder;
  }

  private Order buildOrder(
      UUID customerId, Cart cart, CheckoutUseCase.CheckoutRequestFields request) {
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

  private OrderItem buildOrderItem(UUID orderId, com.eliteshop.colombia.cart.domain.model.CartItem item) {
    var product =
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
}
