package com.eliteshop.colombia.product.domain.model;

import java.util.Objects;
import lombok.Getter;

@Getter
public class ProductName {
  private final String value;

  public ProductName(String value) {
    Objects.requireNonNull(value, "El nombre del producto no puede ser nulo");
    if (value.isBlank()) {
      throw new IllegalArgumentException("El nombre del producto no puede estar vacío");
    }
    this.value = value;
  }
}
