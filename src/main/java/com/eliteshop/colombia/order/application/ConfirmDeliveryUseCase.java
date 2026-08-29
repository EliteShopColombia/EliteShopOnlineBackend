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
public class ConfirmDeliveryUseCase {

  private final OrderRepository repository;
  private final ApplicationEventPublisher eventPublisher;

  public void execute(OrderId orderId) {
    log.info("Confirmando entrega de orden con id: {}", orderId);

    Order order =
        repository
            .findById(orderId)
            .orElseThrow(
                () -> {
                  log.error("Orden no encontrada con id: {}", orderId);
                  return new OrderNotFoundException("The order not exist in our platform");
                });

    OrderStatus currentStatus = order.getStatus();
    if (currentStatus != OrderStatus.OUT_FOR_DELIVERY) {
      log.error("No se puede confirmar entrega de orden {} con estado: {}", orderId, currentStatus);
      throw new InvalidOrderStatusTransitionException(
          "Only OUT_FOR_DELIVERY orders can be confirmed as delivered, current status: "
              + currentStatus);
    }

    Timestamp updatedAt = new Timestamp(System.currentTimeMillis());
    if (!repository.updateStatusIfCurrent(
        orderId, OrderStatus.OUT_FOR_DELIVERY, OrderStatus.DELIVERED, updatedAt)) {
      throw new InvalidOrderStatusTransitionException(
          "The order status changed before the delivery could be confirmed");
    }

    eventPublisher.publishEvent(
        OrderStatusChangedEvent.of(
            order.getId().getValue(),
            order.getCustomerId().getValue(),
            currentStatus.name(),
            OrderStatus.DELIVERED.name()));

    log.info("Entrega de orden {} confirmada exitosamente", orderId);
  }
}
