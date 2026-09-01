package com.eliteshop.colombia.order.domain.model;

import java.util.Set;

public enum OrderStatus {
  PENDING_PAYMENT,
  PAID,
  IN_PREPARATION,
  SHIPPED,
  OUT_FOR_DELIVERY,
  DELIVERED,
  COMPLETED,
  CANCELLED,
  DISPUTE,
  REFUNDED;

  private static final Set<OrderStatus> CANNOT_TRANSITION_AFTER_CANCELLATION =
      Set.of(PENDING_PAYMENT, PAID, IN_PREPARATION);

  public boolean canTransitionTo(OrderStatus next) {
    if (this == next) {
      return false;
    }
    if (this == CANCELLED || this == REFUNDED) {
      return false;
    }
    if (this == COMPLETED) {
      return false;
    }
    return switch (this) {
      case PENDING_PAYMENT -> next == PAID || next == CANCELLED;
      case PAID -> next == IN_PREPARATION || next == CANCELLED || next == DISPUTE;
      case IN_PREPARATION -> next == SHIPPED || next == CANCELLED;
      case SHIPPED -> next == OUT_FOR_DELIVERY || next == DISPUTE;
      case OUT_FOR_DELIVERY -> next == DELIVERED || next == DISPUTE;
      case DELIVERED -> next == COMPLETED || next == DISPUTE;
      case DISPUTE -> next == REFUNDED || next == COMPLETED;
      default -> false;
    };
  }
}
