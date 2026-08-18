package com.eliteshop.colombia.order.application;

import com.eliteshop.colombia.order.domain.model.Order;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderFindAllUseCase {

  private final OrderRepository repository;

  public List<Order> execute() {
    return repository.findAll();
  }
}
