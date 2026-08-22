package com.eliteshop.colombia.order.application;

import com.eliteshop.colombia.order.domain.event.OrderStatusChangedEvent;
import com.eliteshop.colombia.order.domain.exception.InvalidOrderStatusTransitionException;
import com.eliteshop.colombia.order.domain.exception.OrderAccessDeniedException;
import com.eliteshop.colombia.order.domain.exception.OrderNotFoundException;
import com.eliteshop.colombia.order.domain.model.Order;
import com.eliteshop.colombia.order.domain.model.OrderId;
import com.eliteshop.colombia.order.domain.model.OrderStatus;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import java.sql.Timestamp;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

@Slf4j
@RequiredArgsConstructor
public class DisputeOrderUseCase {

  private final OrderRepository repository;
  private final ApplicationEventPublisher eventPublisher;

  private static final Set<OrderStatus> DISPUTABLE_STATUSES =
      Set.of(
          OrderStatus.IN_PREPARATION,
          OrderStatus.SHIPPED,
          OrderStatus.OUT_FOR_DELIVERY,
          OrderStatus.DELIVERED,
          OrderStatus.COMPLETED);

  public void execute(OrderId orderId, UUID actorId) {
    log.info("Abriendo disputa para orden {}", orderId);

    Order order =
        repository
            .findById(orderId)
            .orElseThrow(
                () -> {
                  log.error("Orden no encontrada con id: {}", orderId);
                  return new OrderNotFoundException("The order not exist in our platform");
                });

    if (!DISPUTABLE_STATUSES.contains(order.getStatus())) {
      log.error(
          "No se puede abrir disputa para orden {} con estado: {}", orderId, order.getStatus());
      throw new InvalidOrderStatusTransitionException(
          "Cannot open dispute for order with status: "
              + order.getStatus()
              + ". Disputes are allowed for: IN_PREPARATION, SHIPPED, OUT_FOR_DELIVERY, DELIVERED, COMPLETED");
    }

    if (!order.getCustomerId().getValue().equals(actorId)) {
      throw new OrderAccessDeniedException("The authenticated user cannot dispute this order");
    }

    Timestamp updatedAt = new Timestamp(System.currentTimeMillis());
    if (!repository.updateStatusIfCurrent(
        orderId, order.getStatus(), OrderStatus.DISPUTE, updatedAt)) {
      throw new InvalidOrderStatusTransitionException(
          "The order status changed before the dispute was opened");
    }

    eventPublisher.publishEvent(
        OrderStatusChangedEvent.of(
            order.getId().getValue(),
            order.getCustomerId().getValue(),
            order.getStatus().name(),
            OrderStatus.DISPUTE.name()));

    log.info("Disputa abierta para orden {}", orderId);
  }
}
