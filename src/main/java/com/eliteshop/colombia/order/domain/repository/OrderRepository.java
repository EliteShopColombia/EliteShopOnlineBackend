package com.eliteshop.colombia.order.domain.repository;

import com.eliteshop.colombia.order.domain.model.Order;
import com.eliteshop.colombia.order.domain.model.OrderId;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {
  Order save(Order order);

  void update(Order order);

  void delete(OrderId id);

  List<Order> findAll();

  Optional<Order> findById(OrderId id);
}
