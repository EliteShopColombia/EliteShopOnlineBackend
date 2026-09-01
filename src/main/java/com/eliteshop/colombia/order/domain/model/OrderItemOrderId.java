package com.eliteshop.colombia.order.domain.model;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class OrderItemOrderId {
  private final UUID value;

  public OrderItemOrderId(UUID value) {
    Objects.requireNonNull(value, "El ID de la orden no puede ser nulo");
    this.value = value;
  }
}
