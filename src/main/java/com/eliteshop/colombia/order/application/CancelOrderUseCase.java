package com.eliteshop.colombia.order.application;

import com.eliteshop.colombia.order.domain.event.OrderStatusChangedEvent;
import com.eliteshop.colombia.order.domain.exception.InvalidOrderStatusTransitionException;
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
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

@Slf4j
@RequiredArgsConstructor
public class CancelOrderUseCase {

  private final OrderRepository repository;
  private final OrderItemRepository orderItemRepository;
  private final ProductRepository productRepository;
  private final ApplicationEventPublisher eventPublisher;

  private static final Set<OrderStatus> CANCELLABLE_STATUSES =
      Set.of(OrderStatus.PENDING_PAYMENT, OrderStatus.PAID);

  public void execute(OrderId orderId) {
    log.info("Cancelando orden con id: {}", orderId);

    Order order =
        repository
            .findById(orderId)
            .orElseThrow(
                () -> {
                  log.error("Orden no encontrada con id: {}", orderId);
                  return new OrderNotFoundException("The order not exist in our platform");
                });

    OrderStatus currentStatus = order.getStatus();
    if (!CANCELLABLE_STATUSES.contains(currentStatus)) {
      log.error("No se puede cancelar orden {} con estado: {}", orderId, currentStatus);
      throw new InvalidOrderStatusTransitionException(
          "Only PENDING_PAYMENT or PAID orders can be cancelled, current status: " + currentStatus);
    }

    Timestamp updatedAt = new Timestamp(System.currentTimeMillis());
    if (!repository.updateStatusIfCurrent(
        orderId, currentStatus, OrderStatus.CANCELLED, updatedAt)) {
      throw new InvalidOrderStatusTransitionException(
          "The order status changed before the cancellation was processed");
    }

    restoreStock(order);

    eventPublisher.publishEvent(
        OrderStatusChangedEvent.of(
            order.getId().getValue(),
            order.getCustomerId().getValue(),
            currentStatus.name(),
            OrderStatus.CANCELLED.name()));

    log.info("Orden {} cancelada exitosamente", orderId);
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
