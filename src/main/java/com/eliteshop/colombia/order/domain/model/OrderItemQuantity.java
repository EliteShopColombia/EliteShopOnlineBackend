package com.eliteshop.colombia.order.domain.model;

import java.util.Objects;
import lombok.Getter;

@Getter
public class OrderItemQuantity {
  private final Integer value;

  public OrderItemQuantity(Integer value) {
    Objects.requireNonNull(value, "La cantidad no puede ser nula");
    if (value < 1) {
      throw new IllegalArgumentException("La cantidad debe ser al menos 1");
    }
    this.value = value;
  }
}
