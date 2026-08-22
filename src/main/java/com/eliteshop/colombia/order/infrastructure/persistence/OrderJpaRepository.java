package com.eliteshop.colombia.order.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {
  List<OrderEntity> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

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
