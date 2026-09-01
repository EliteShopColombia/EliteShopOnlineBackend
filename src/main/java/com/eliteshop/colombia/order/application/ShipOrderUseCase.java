package com.eliteshop.colombia.order.application;

import com.eliteshop.colombia.order.domain.event.OrderStatusChangedEvent;
import com.eliteshop.colombia.order.domain.exception.InvalidOrderStatusTransitionException;
import com.eliteshop.colombia.order.domain.exception.OrderNotFoundException;
import com.eliteshop.colombia.order.domain.model.Order;
import com.eliteshop.colombia.order.domain.model.OrderId;
import com.eliteshop.colombia.order.domain.model.OrderStatus;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import java.sql.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

@Slf4j
@RequiredArgsConstructor
public class ShipOrderUseCase {

  private final OrderRepository repository;
  private final ApplicationEventPublisher eventPublisher;

  public void execute(OrderId orderId) {
    log.info("Marcando orden {} como enviada", orderId);

    Order order =
        repository
            .findById(orderId)
            .orElseThrow(
                () -> {
                  log.error("Orden no encontrada con id: {}", orderId);
                  return new OrderNotFoundException("The order not exist in our platform");
                });

    if (order.getStatus() != OrderStatus.IN_PREPARATION) {
      log.error("No se puede enviar orden {} con estado: {}", orderId, order.getStatus());
      throw new InvalidOrderStatusTransitionException(
          "Only IN_PREPARATION orders can be shipped, current status: " + order.getStatus());
    }

    Timestamp updatedAt = new Timestamp(System.currentTimeMillis());
    if (!repository.updateStatusIfCurrent(
        orderId, OrderStatus.IN_PREPARATION, OrderStatus.SHIPPED, updatedAt)) {
      throw new InvalidOrderStatusTransitionException(
          "The order status changed before it could be shipped");
    }

    eventPublisher.publishEvent(
        OrderStatusChangedEvent.of(
            order.getId().getValue(),
            order.getCustomerId().getValue(),
            OrderStatus.IN_PREPARATION.name(),
            OrderStatus.SHIPPED.name()));

    log.info("Orden {} marcada como enviada", orderId);
  }
}
