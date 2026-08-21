package com.eliteshop.colombia.order.infrastructure.persistence.tracking;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "tracking_events")
public class TrackingEventEntity {

  @Id
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "order_id", nullable = false)
  private UUID orderId;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "location")
  private String location;

  @Column(name = "description")
  private String description;

  @Column(name = "event_timestamp", nullable = false)
  private Timestamp eventTimestamp;

  @Column(name = "created_at", nullable = false)
  private Timestamp createdAt;
}
