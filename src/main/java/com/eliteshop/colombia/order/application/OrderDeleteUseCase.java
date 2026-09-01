package com.eliteshop.colombia.order.application;

import com.eliteshop.colombia.order.domain.exception.OrderNotFoundException;
import com.eliteshop.colombia.order.domain.model.OrderId;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class OrderDeleteUseCase {

  private final OrderRepository repository;

  public void execute(OrderId id) {
    log.info("Iniciando eliminación de orden con id: {}", id);

    if (!repository.findById(id).isPresent()) {
      log.error("Orden no encontrada con id: {}", id);
      throw new OrderNotFoundException("The order not exist in our platform");
    }

    this.repository.delete(id);
    log.info("Orden eliminada exitosamente con id: {}", id);
  }
}
