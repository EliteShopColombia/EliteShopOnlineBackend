package com.eliteshop.colombia.cart.domain.model;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class CartCustomerId {
  private final UUID value;

  public CartCustomerId(UUID value) {
    Objects.requireNonNull(value, "El ID del customer no puede ser nulo");
    this.value = value;
  }
}
