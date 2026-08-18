package com.eliteshop.colombia.order.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderCreatedEvent(
    UUID orderId, UUID customerId, BigDecimal totalAmount, Instant occurredAt) {

  public static OrderCreatedEvent of(UUID orderId, UUID customerId, BigDecimal totalAmount) {
    return new OrderCreatedEvent(orderId, customerId, totalAmount, Instant.now());
  }
}
