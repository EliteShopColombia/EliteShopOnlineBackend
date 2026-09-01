package com.eliteshop.colombia.order.application;

import com.eliteshop.colombia.order.domain.event.OrderStatusChangedEvent;
import com.eliteshop.colombia.order.domain.exception.InvalidOrderStatusTransitionException;
import com.eliteshop.colombia.order.domain.exception.OrderNotFoundException;
import com.eliteshop.colombia.order.domain.model.Order;
import com.eliteshop.colombia.order.domain.model.OrderStatus;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

@Slf4j
@RequiredArgsConstructor
public class OrderUpdateUseCase {

  private final OrderRepository repository;
  private final ApplicationEventPublisher eventPublisher;

  private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS =
      Map.of(
          OrderStatus.PENDING_PAYMENT,
          Set.of(OrderStatus.PAID, OrderStatus.CANCELLED),
          OrderStatus.PAID,
          Set.of(OrderStatus.IN_PREPARATION, OrderStatus.CANCELLED),
          OrderStatus.IN_PREPARATION,
          Set.of(OrderStatus.SHIPPED, OrderStatus.DISPUTE, OrderStatus.CANCELLED),
          OrderStatus.SHIPPED,
          Set.of(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.DISPUTE),
          OrderStatus.OUT_FOR_DELIVERY,
          Set.of(OrderStatus.DELIVERED, OrderStatus.DISPUTE),
          OrderStatus.DELIVERED,
          Set.of(OrderStatus.COMPLETED, OrderStatus.DISPUTE),
          OrderStatus.COMPLETED,
          Set.of(OrderStatus.DISPUTE),
          OrderStatus.DISPUTE,
          Set.of(OrderStatus.REFUNDED),
          OrderStatus.CANCELLED,
          Set.of(),
          OrderStatus.REFUNDED,
          Set.of());

  public Order execute(Order order) {
    log.info("Actualizando orden con id: {}", order.getId());

    if (order.getId() == null || repository.findById(order.getId()).isEmpty()) {
      log.error("Orden no encontrada con id: {}", order.getId());
      throw new OrderNotFoundException("The order not exist in our platform");
    }

    Order existingOrder = repository.findById(order.getId()).orElseThrow();
    OrderStatus previousStatus = existingOrder.getStatus();
    OrderStatus newStatus = order.getStatus();

    if (previousStatus != newStatus) {
      Set<OrderStatus> allowed = VALID_TRANSITIONS.getOrDefault(previousStatus, Set.of());
      if (!allowed.contains(newStatus)) {
        throw new InvalidOrderStatusTransitionException(
            "Invalid status transition from "
                + previousStatus
                + " to "
                + newStatus
                + ". Allowed: "
                + allowed);
      }

      Timestamp updatedAt = new Timestamp(System.currentTimeMillis());
      if (!repository.updateStatusIfCurrent(order.getId(), previousStatus, newStatus, updatedAt)) {
        throw new InvalidOrderStatusTransitionException(
            "The order status changed before the update was processed");
      }

      eventPublisher.publishEvent(
          OrderStatusChangedEvent.of(
              order.getId().getValue(),
              order.getCustomerId().getValue(),
              previousStatus.name(),
              newStatus.name()));
      log.info(
          "Estado de orden cambiado de {} a {} para orden id: {}",
          previousStatus,
          newStatus,
          order.getId());
    }

    log.info("Orden actualizada exitosamente con id: {}", order.getId());
    return repository.findById(order.getId()).orElseThrow();
  }
}
