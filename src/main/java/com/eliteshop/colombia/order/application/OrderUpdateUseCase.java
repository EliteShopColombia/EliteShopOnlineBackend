package com.eliteshop.colombia.order.application;

import com.eliteshop.colombia.order.domain.event.OrderStatusChangedEvent;
import com.eliteshop.colombia.order.domain.exception.OrderNotFoundException;
import com.eliteshop.colombia.order.domain.model.Order;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

@Slf4j
@RequiredArgsConstructor
public class OrderUpdateUseCase {

  private final OrderRepository repository;
  private final ApplicationEventPublisher eventPublisher;

  public void execute(Order order) {
    log.info("Actualizando orden con id: {}", order.getId());

    if (order.getId() == null || repository.findById(order.getId()).isEmpty()) {
      log.error("Orden no encontrada con id: {}", order.getId());
      throw new OrderNotFoundException("The order not exist in our platform");
    }

    Order existingOrder = repository.findById(order.getId()).orElseThrow();
    String previousStatus = existingOrder.getStatus().name();

    this.repository.update(order);

    if (!previousStatus.equals(order.getStatus().name())) {
      eventPublisher.publishEvent(
          OrderStatusChangedEvent.of(
              order.getId().getValue(),
              order.getCustomerId().getValue(),
              previousStatus,
              order.getStatus().name()));
      log.info(
          "Estado de orden cambiado de {} a {} para orden id: {}",
          previousStatus,
          order.getStatus().name(),
          order.getId());
    }

    log.info("Orden actualizada exitosamente con id: {}", order.getId());
  }
}
