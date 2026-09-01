package com.eliteshop.colombia.order.application;

import com.eliteshop.colombia.order.domain.event.OrderCreatedEvent;
import com.eliteshop.colombia.order.domain.exception.OrderAlreadyExistsException;
import com.eliteshop.colombia.order.domain.model.Order;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

@Slf4j
@RequiredArgsConstructor
public class OrderSaveUseCase {

  private final OrderRepository repository;
  private final ApplicationEventPublisher eventPublisher;

  public void execute(Order order) {
    log.info("Guardando orden con id: {}", order.getId());

    repository
        .findById(order.getId())
        .ifPresent(
            existingOrder -> {
              log.error("Ya existe una orden con id: {}", order.getId());
              throw new OrderAlreadyExistsException("This order already exist in the platform");
            });

    this.repository.save(order);

    eventPublisher.publishEvent(
        OrderCreatedEvent.of(
            order.getId().getValue(),
            order.getCustomerId().getValue(),
            order.getTotalAmount().getValue()));

    log.info("Orden guardada exitosamente con id: {}", order.getId());
  }
}
