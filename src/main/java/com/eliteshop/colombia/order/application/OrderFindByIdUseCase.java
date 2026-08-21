package com.eliteshop.colombia.order.application;

import com.eliteshop.colombia.order.domain.model.Order;
import com.eliteshop.colombia.order.domain.model.OrderId;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class OrderFindByIdUseCase {

  private final OrderRepository repository;

  public Optional<Order> execute(OrderId id) {
    log.info("Buscando orden por id: {}", id);

    if (id == null) {
      log.warn("Se recibió id nulo en OrderFindByIdUseCase");
      return Optional.empty();
    }

    Optional<Order> order = repository.findById(id);
    if (order.isPresent()) {
      log.info("Orden encontrada con id: {}", id);
    } else {
      log.warn("Orden no encontrada con id: {}", id);
    }
    return order;
  }
}
