package com.eliteshop.colombia.cart.domain.model;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class CartId {
  private final UUID value;

  public CartId(UUID value) {
    Objects.requireNonNull(value, "El ID del carrito no puede ser nulo");
    this.value = value;
  }

  public static CartId generate() {
    return new CartId(UUID.randomUUID());
  }
}
