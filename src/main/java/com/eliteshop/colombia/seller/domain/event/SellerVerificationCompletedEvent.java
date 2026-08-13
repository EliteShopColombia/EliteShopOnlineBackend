package com.eliteshop.colombia.seller.domain.event;

import java.time.Instant;
import java.util.UUID;

public record SellerVerificationCompletedEvent(
    UUID sellerId, boolean verified, double confidence, String message, Instant occurredAt) {

  public static SellerVerificationCompletedEvent approved(UUID sellerId, double confidence) {
    return new SellerVerificationCompletedEvent(
        sellerId, true, confidence, "Identidad verificada", Instant.now());
  }

  public static SellerVerificationCompletedEvent rejected(UUID sellerId, String message) {
    return new SellerVerificationCompletedEvent(sellerId, false, 0.0, message, Instant.now());
  }
}
