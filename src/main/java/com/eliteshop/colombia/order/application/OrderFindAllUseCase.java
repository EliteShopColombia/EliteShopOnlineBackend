package com.eliteshop.colombia.order.application;

import com.eliteshop.colombia.order.domain.model.Order;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class OrderFindAllUseCase {

  private final OrderRepository repository;

  public List<Order> execute() {
    log.info("Buscando todas las órdenes");
    List<Order> orders = repository.findAll();
    log.info("Se encontraron {} órdenes", orders.size());
    return orders;
  }
}
