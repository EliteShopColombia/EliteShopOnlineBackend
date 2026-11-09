package com.eliteshop.colombia.product.domain.model;

import java.math.BigDecimal;
import java.util.Objects;
import lombok.Getter;

@Getter
public class ProductPrice {
  private final BigDecimal value;

  public ProductPrice(BigDecimal value) {
    Objects.requireNonNull(value, "Precio no puede ser null");
    if (value.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("El precio debe ser mayor que cero: " + value);
    }
    this.value = value;
  }
}
