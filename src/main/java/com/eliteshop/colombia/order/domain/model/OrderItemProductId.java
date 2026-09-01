package com.eliteshop.colombia.order.domain.model;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class OrderItemProductId {
  private final UUID value;

  public OrderItemProductId(UUID value) {
    Objects.requireNonNull(value, "El ID del producto no puede ser nulo");
    this.value = value;
  }
}
