package com.eliteshop.colombia.order.domain.model;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class OrderItemId {
  private final UUID value;

  public OrderItemId(UUID value) {
    Objects.requireNonNull(value, "El ID del item no puede ser nulo");
    this.value = value;
  }

  public static OrderItemId generate() {
    return new OrderItemId(UUID.randomUUID());
  }
}
