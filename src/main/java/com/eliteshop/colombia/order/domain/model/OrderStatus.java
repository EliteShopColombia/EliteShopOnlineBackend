package com.eliteshop.colombia.order.domain.model;

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
  REFUNDED
}
