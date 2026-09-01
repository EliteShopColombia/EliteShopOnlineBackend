package com.eliteshop.colombia.order.domain.repository;

import com.eliteshop.colombia.order.domain.model.OrderItem;
import java.util.List;
import java.util.UUID;

public interface OrderItemRepository {
  OrderItem save(OrderItem orderItem);

  List<OrderItem> saveAll(List<OrderItem> orderItems);

  List<OrderItem> findByOrderId(UUID orderId);

  void deleteByOrderId(UUID orderId);

  boolean existsVerifiedPurchase(UUID customerId, UUID productId);

  List<UUID> findDistinctOrderIdsBySellerId(UUID sellerId);
}
