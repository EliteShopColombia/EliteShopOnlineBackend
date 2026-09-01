package com.eliteshop.colombia.order.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {
  List<OrderEntity> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

  Page<OrderEntity> findByCustomerIdOrderByCreatedAtDesc(UUID customerId, Pageable pageable);

  @Query(
      "SELECT DISTINCT o FROM OrderEntity o JOIN OrderItemEntity oi ON o.id = oi.orderId"
          + " WHERE oi.sellerId = :sellerId")
  Page<OrderEntity> findPageBySellerId(@Param("sellerId") UUID sellerId, Pageable pageable);

  @Modifying
  @Transactional
  @Query(
      "UPDATE OrderEntity o SET o.status = :newStatus, o.updatedAt = :updatedAt"
          + " WHERE o.id = :orderId AND o.status = :currentStatus")
  int updateStatusIfCurrent(
      @Param("orderId") UUID orderId,
      @Param("currentStatus") String currentStatus,
      @Param("newStatus") String newStatus,
      @Param("updatedAt") java.sql.Timestamp updatedAt);
}
