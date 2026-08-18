package com.eliteshop.colombia.order.application;

import com.eliteshop.colombia.order.domain.model.Order;
import com.eliteshop.colombia.order.domain.model.OrderId;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderFindByIdUseCase {

  private final OrderRepository repository;

  public Optional<Order> execute(OrderId id) {
    if (id == null) return Optional.empty();

    return repository.findById(id);
  }
}
