package com.eliteshop.colombia.order.domain.event;

import java.time.Instant;
import java.util.UUID;

public record OrderStatusChangedEvent(
    UUID orderId, UUID customerId, String previousStatus, String newStatus, Instant occurredAt) {

  public static OrderStatusChangedEvent of(
      UUID orderId, UUID customerId, String previousStatus, String newStatus) {
    return new OrderStatusChangedEvent(
        orderId, customerId, previousStatus, newStatus, Instant.now());
  }
}
