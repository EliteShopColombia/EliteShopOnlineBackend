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
public class OutForDeliveryUseCase {

  private final OrderRepository repository;
  private final ApplicationEventPublisher eventPublisher;

  public void execute(OrderId orderId) {
    log.info("Marcando orden {} en reparto", orderId);

    Order order =
        repository
            .findById(orderId)
            .orElseThrow(
                () -> {
                  log.error("Orden no encontrada con id: {}", orderId);
                  return new OrderNotFoundException("The order not exist in our platform");
                });

    if (order.getStatus() != OrderStatus.SHIPPED) {
      log.error(
          "No se puede marcar en reparto orden {} con estado: {}", orderId, order.getStatus());
      throw new InvalidOrderStatusTransitionException(
          "Only SHIPPED orders can be out for delivery, current status: " + order.getStatus());
    }

    Order outForDeliveryOrder =
        new Order(
            order.getId(),
            order.getCustomerId(),
            OrderStatus.OUT_FOR_DELIVERY,
            order.getTotalAmount(),
            order.getShippingAddress(),
            order.getShippingDepartment(),
            order.getShippingCity(),
            order.getCreatedAt(),
            new OrderUpdatedAt(new Timestamp(System.currentTimeMillis())),
            order.getTrackingNumber(),
            order.getShippingCarrier(),
            order.getShippingLabelUrl());

    repository.update(outForDeliveryOrder);

    eventPublisher.publishEvent(
        OrderStatusChangedEvent.of(
            order.getId().getValue(),
            order.getCustomerId().getValue(),
            OrderStatus.SHIPPED.name(),
            OrderStatus.OUT_FOR_DELIVERY.name()));

    log.info("Orden {} marcada en reparto", orderId);
  }
}
