package com.eliteshop.colombia.seller.domain.event;

import java.time.Instant;
import java.util.UUID;

public record SellerCreatedEvent(UUID sellerId, String email, Instant occurredAt) {

  public static SellerCreatedEvent of(UUID sellerId, String email) {
    return new SellerCreatedEvent(sellerId, email, Instant.now());
  }
}
