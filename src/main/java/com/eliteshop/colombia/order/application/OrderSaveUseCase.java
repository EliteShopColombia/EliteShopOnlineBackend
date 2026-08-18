package com.eliteshop.colombia.order.application;

import com.eliteshop.colombia.order.domain.event.OrderCreatedEvent;
import com.eliteshop.colombia.order.domain.exception.OrderAlreadyExistsException;
import com.eliteshop.colombia.order.domain.model.Order;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;

@RequiredArgsConstructor
public class OrderSaveUseCase {

  private final OrderRepository repository;
  private final ApplicationEventPublisher eventPublisher;

  public void execute(Order order) {
    repository
        .findById(order.getId())
        .ifPresent(
            existingOrder -> {
              throw new OrderAlreadyExistsException("This order already exist in the platform");
            });

    this.repository.save(order);

    eventPublisher.publishEvent(
        OrderCreatedEvent.of(
            order.getId().getValue(),
            order.getCustomerId().getValue(),
            order.getTotalAmount().getValue()));
  }
}
