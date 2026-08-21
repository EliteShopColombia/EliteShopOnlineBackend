package com.eliteshop.colombia.order.application;

import com.eliteshop.colombia.order.domain.event.OrderStatusChangedEvent;
import com.eliteshop.colombia.order.domain.exception.InvalidOrderStatusTransitionException;
import com.eliteshop.colombia.order.domain.exception.OrderNotFoundException;
import com.eliteshop.colombia.order.domain.model.Order;
import com.eliteshop.colombia.order.domain.model.OrderId;
import com.eliteshop.colombia.order.domain.model.OrderStatus;
import com.eliteshop.colombia.order.domain.model.OrderUpdatedAt;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import java.sql.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

@Slf4j
@RequiredArgsConstructor
public class CancelOrderUseCase {

  private final OrderRepository repository;
  private final ApplicationEventPublisher eventPublisher;

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
    if (currentStatus != OrderStatus.PENDING_PAYMENT && currentStatus != OrderStatus.PAID) {
      log.error("No se puede cancelar orden {} con estado: {}", orderId, currentStatus);
      throw new InvalidOrderStatusTransitionException(
          "Only PENDING_PAYMENT or PAID orders can be cancelled, current status: " + currentStatus);
    }

    Order cancelledOrder =
        new Order(
            order.getId(),
            order.getCustomerId(),
            OrderStatus.CANCELLED,
            order.getTotalAmount(),
            order.getShippingAddress(),
            order.getShippingDepartment(),
            order.getShippingCity(),
            order.getCreatedAt(),
            new OrderUpdatedAt(new Timestamp(System.currentTimeMillis())),
            order.getTrackingNumber(),
            order.getShippingCarrier(),
            order.getShippingLabelUrl());

    repository.update(cancelledOrder);

    eventPublisher.publishEvent(
        OrderStatusChangedEvent.of(
            order.getId().getValue(),
            order.getCustomerId().getValue(),
            currentStatus.name(),
            OrderStatus.CANCELLED.name()));

    log.info("Orden {} cancelada exitosamente", orderId);
  }
}
