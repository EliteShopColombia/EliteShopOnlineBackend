package com.eliteshop.colombia.order.application;

import com.eliteshop.colombia.order.domain.model.Order;
import com.eliteshop.colombia.order.domain.model.OrderCustomerId;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class FindOrdersByCustomerIdUseCase {

  private final OrderRepository repository;

  public List<Order> execute(OrderCustomerId customerId) {
    log.info("Buscando ordenes por customerId: {}", customerId.getValue());
    List<Order> orders = repository.findByCustomerId(customerId.getValue());
    log.info("Se encontraron {} ordenes para customerId: {}", orders.size(), customerId.getValue());
    return orders;
  }
}
