package com.eliteshop.colombia.cart.domain.model;

import java.util.Objects;
import lombok.Getter;

@Getter
public class CartItemQuantity {
  private final Integer value;

  public CartItemQuantity(Integer value) {
    Objects.requireNonNull(value, "La cantidad no puede ser nula");
    if (value < 1) {
      throw new IllegalArgumentException("La cantidad debe ser al menos 1");
    }
    this.value = value;
  }
}
