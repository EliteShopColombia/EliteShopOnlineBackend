package com.eliteshop.colombia.order.application;

import com.eliteshop.colombia.order.domain.event.OrderStatusChangedEvent;
import com.eliteshop.colombia.order.domain.exception.OrderNotFoundException;
import com.eliteshop.colombia.order.domain.model.Order;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;

@RequiredArgsConstructor
public class OrderUpdateUseCase {

  private final OrderRepository repository;
  private final ApplicationEventPublisher eventPublisher;

  public void execute(Order order) {
    if (order.getId() == null || repository.findById(order.getId()).isEmpty()) {
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
    }
  }
}
