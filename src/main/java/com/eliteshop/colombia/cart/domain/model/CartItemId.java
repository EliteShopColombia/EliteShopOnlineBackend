package com.eliteshop.colombia.cart.domain.model;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class CartItemId {
  private final UUID value;

  public CartItemId(UUID value) {
    Objects.requireNonNull(value, "El ID del item no puede ser nulo");
    this.value = value;
  }

  public static CartItemId generate() {
    return new CartItemId(UUID.randomUUID());
  }
}
