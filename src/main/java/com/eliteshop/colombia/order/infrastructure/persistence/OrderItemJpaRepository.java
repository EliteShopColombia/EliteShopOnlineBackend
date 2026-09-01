package com.eliteshop.colombia.order.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemJpaRepository extends JpaRepository<OrderItemEntity, UUID> {
  List<OrderItemEntity> findByOrderId(UUID orderId);

  void deleteByOrderId(UUID orderId);

  @Query("SELECT DISTINCT oi.orderId FROM OrderItemEntity oi WHERE oi.sellerId = :sellerId")
  List<UUID> findDistinctOrderIdsBySellerId(@Param("sellerId") UUID sellerId);

  @Query(
      "SELECT COUNT(oi) > 0 FROM OrderItemEntity oi"
          + " JOIN OrderEntity o ON oi.orderId = o.id"
          + " WHERE o.customerId = :customerId"
          + " AND oi.productId = :productId"
          + " AND o.status IN ('PAID', 'IN_PREPARATION', 'SHIPPED', 'OUT_FOR_DELIVERY', 'DELIVERED', 'COMPLETED')")
  boolean existsVerifiedPurchase(
      @Param("customerId") UUID customerId, @Param("productId") UUID productId);
}
