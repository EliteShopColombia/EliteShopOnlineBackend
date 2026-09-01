package com.eliteshop.colombia.order.application;

import com.eliteshop.colombia.order.domain.event.OrderStatusChangedEvent;
import com.eliteshop.colombia.order.domain.exception.InvalidOrderStatusTransitionException;
import com.eliteshop.colombia.order.domain.exception.OrderAccessDeniedException;
import com.eliteshop.colombia.order.domain.exception.OrderNotFoundException;
import com.eliteshop.colombia.order.domain.model.Order;
import com.eliteshop.colombia.order.domain.model.OrderId;
import com.eliteshop.colombia.order.domain.model.OrderItem;
import com.eliteshop.colombia.order.domain.model.OrderStatus;
import com.eliteshop.colombia.order.domain.repository.OrderItemRepository;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import com.eliteshop.colombia.product.domain.model.ProductId;
import com.eliteshop.colombia.product.domain.repository.ProductRepository;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

@Slf4j
@RequiredArgsConstructor
public class RefundOrderUseCase {

  private final OrderRepository repository;
  private final OrderItemRepository orderItemRepository;
  private final ProductRepository productRepository;
  private final ApplicationEventPublisher eventPublisher;

  public void execute(OrderId orderId, UUID actorId) {
    log.info("Procesando reembolso para orden {}", orderId);

    Order order =
        repository
            .findById(orderId)
            .orElseThrow(
                () -> {
                  log.error("Orden no encontrada con id: {}", orderId);
                  return new OrderNotFoundException("The order not exist in our platform");
                });

    if (order.getStatus() != OrderStatus.DISPUTE) {
      log.error(
          "No se puede procesar reembolso para orden {} con estado: {}",
          orderId,
          order.getStatus());
      throw new InvalidOrderStatusTransitionException(
          "Only DISPUTE orders can be refunded, current status: " + order.getStatus());
    }

    if (!order.getCustomerId().getValue().equals(actorId)) {
      throw new OrderAccessDeniedException("The authenticated user cannot refund this order");
    }

    Timestamp updatedAt = new Timestamp(System.currentTimeMillis());
    if (!repository.updateStatusIfCurrent(
        orderId, OrderStatus.DISPUTE, OrderStatus.REFUNDED, updatedAt)) {
      throw new InvalidOrderStatusTransitionException(
          "The order status changed before the refund was processed");
    }

    if (shouldRestoreStock(order)) {
      restoreStock(order);
    } else {
      log.info(
          "Stock no restaurado para orden {} - motivo de disputa no permite restock: {}",
          orderId,
          order.getDisputeReason());
    }

    eventPublisher.publishEvent(
        OrderStatusChangedEvent.of(
            order.getId().getValue(),
            order.getCustomerId().getValue(),
            OrderStatus.DISPUTE.name(),
            OrderStatus.REFUNDED.name()));

    log.info("Reembolso procesado para orden {}", orderId);
  }

  private boolean shouldRestoreStock(Order order) {
    if (order.getDisputeReason() == null) {
      return true;
    }
    return order.getDisputeReason().isRestockable();
  }

  private void restoreStock(Order order) {
    try {
      List<OrderItem> items = orderItemRepository.findByOrderId(order.getId().getValue());
      for (OrderItem item : items) {
        productRepository.restoreStock(
            new ProductId(item.getProductId().getValue()), item.getQuantity().getValue());
      }
      log.info("Stock restaurado para {} items de orden {}", items.size(), order.getId());
    } catch (Exception e) {
      log.error("Error restaurando stock para orden {}: {}", order.getId(), e.getMessage());
    }
  }
}
