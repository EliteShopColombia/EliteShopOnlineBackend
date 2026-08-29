package com.eliteshop.colombia.order.domain.repository;

import com.eliteshop.colombia.order.domain.model.DisputeReason;
import com.eliteshop.colombia.order.domain.model.Order;
import com.eliteshop.colombia.order.domain.model.OrderId;
import com.eliteshop.colombia.order.domain.model.OrderStatus;
import com.eliteshop.colombia.shared.domain.PageResult;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {
  Order save(Order order);

  void update(Order order);

  boolean updateStatusIfCurrent(
      OrderId orderId, OrderStatus currentStatus, OrderStatus newStatus, Timestamp updatedAt);

  void delete(OrderId id);

  List<Order> findAll();

  Optional<Order> findById(OrderId id);

  List<Order> findByCustomerId(java.util.UUID customerId);

  PageResult<Order> findPageByCustomerId(java.util.UUID customerId, int page, int size);

  PageResult<Order> findPageBySellerId(java.util.UUID sellerId, int page, int size);

  List<Order> findAllByIds(List<java.util.UUID> ids);

  void updateDisputeReason(OrderId orderId, DisputeReason reason);
}
