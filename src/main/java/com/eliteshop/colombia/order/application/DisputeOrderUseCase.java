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
import java.util.Set;
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
          OrderStatus.DELIVERED);

  public void execute(OrderId orderId) {
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
              + ". Disputes are allowed for: IN_PREPARATION, SHIPPED, OUT_FOR_DELIVERY, DELIVERED");
    }

    Order disputedOrder =
        new Order(
            order.getId(),
            order.getCustomerId(),
            OrderStatus.DISPUTE,
            order.getTotalAmount(),
            order.getShippingAddress(),
            order.getShippingDepartment(),
            order.getShippingCity(),
            order.getCreatedAt(),
            new OrderUpdatedAt(new Timestamp(System.currentTimeMillis())),
            order.getTrackingNumber(),
            order.getShippingCarrier(),
            order.getShippingLabelUrl());

    repository.update(disputedOrder);

    eventPublisher.publishEvent(
        OrderStatusChangedEvent.of(
            order.getId().getValue(),
            order.getCustomerId().getValue(),
            order.getStatus().name(),
            OrderStatus.DISPUTE.name()));

    log.info("Disputa abierta para orden {}", orderId);
  }
}
