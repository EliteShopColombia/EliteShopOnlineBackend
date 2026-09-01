package com.eliteshop.colombia.order.infrastructure.persistence.tracking;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackingEventJpaRepository extends JpaRepository<TrackingEventEntity, UUID> {
  List<TrackingEventEntity> findByOrderIdOrderByEventTimestampAsc(UUID orderId);
}
