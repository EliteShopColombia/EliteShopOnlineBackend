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
public class CompleteOrderUseCase {

  private final OrderRepository repository;
  private final ApplicationEventPublisher eventPublisher;

  public void execute(OrderId orderId) {
    log.info("Completando orden {}", orderId);

    Order order =
        repository
            .findById(orderId)
            .orElseThrow(
                () -> {
                  log.error("Orden no encontrada con id: {}", orderId);
                  return new OrderNotFoundException("The order not exist in our platform");
                });

    if (order.getStatus() != OrderStatus.DELIVERED) {
      log.error("No se puede completar orden {} con estado: {}", orderId, order.getStatus());
      throw new InvalidOrderStatusTransitionException(
          "Only DELIVERED orders can be completed, current status: " + order.getStatus());
    }

    Order completedOrder =
        new Order(
            order.getId(),
            order.getCustomerId(),
            OrderStatus.COMPLETED,
            order.getTotalAmount(),
            order.getShippingAddress(),
            order.getShippingDepartment(),
            order.getShippingCity(),
            order.getCreatedAt(),
            new OrderUpdatedAt(new Timestamp(System.currentTimeMillis())),
            order.getTrackingNumber(),
            order.getShippingCarrier(),
            order.getShippingLabelUrl());

    repository.update(completedOrder);

    eventPublisher.publishEvent(
        OrderStatusChangedEvent.of(
            order.getId().getValue(),
            order.getCustomerId().getValue(),
            OrderStatus.DELIVERED.name(),
            OrderStatus.COMPLETED.name()));

    log.info("Orden {} completada exitosamente", orderId);
  }
}
