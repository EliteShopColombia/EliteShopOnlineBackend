package com.eliteshop.colombia.cart.domain.model;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class CartItemProductId {
  private final UUID value;

  public CartItemProductId(UUID value) {
    Objects.requireNonNull(value, "El ID del producto no puede ser nulo");
    this.value = value;
  }
}
