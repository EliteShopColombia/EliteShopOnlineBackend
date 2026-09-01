package com.eliteshop.colombia.product.domain.model;

import java.util.Objects;
import lombok.Getter;

@Getter
public class ProductName {
  private final String value;

  public ProductName(String value) {
    Objects.requireNonNull(value, "Nombre del producto no puede ser null");
    if (value.isBlank()) {
      throw new IllegalArgumentException("Nombre del producto no puede estar vacio");
    }
    this.value = value;
  }
}
