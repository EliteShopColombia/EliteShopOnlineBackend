package com.eliteshop.colombia.order.application;

import com.eliteshop.colombia.order.domain.exception.OrderNotFoundException;
import com.eliteshop.colombia.order.domain.model.OrderId;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderDeleteUseCase {

  private final OrderRepository repository;

  public void execute(OrderId id) {
    if (!repository.findById(id).isPresent()) {
      throw new OrderNotFoundException("The order not exist in our platform");
    }

    this.repository.delete(id);
  }
}
