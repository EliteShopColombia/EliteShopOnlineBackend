package com.eliteshop.colombia.cart.domain.model;

import java.math.BigDecimal;
import java.util.Objects;
import lombok.Getter;

@Getter
public class CartItemUnitPrice {
  private final BigDecimal value;

  public CartItemUnitPrice(BigDecimal value) {
    Objects.requireNonNull(value, "El precio no puede ser nulo");
    if (value.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("El precio no puede ser negativo");
    }
    this.value = value;
  }
}
