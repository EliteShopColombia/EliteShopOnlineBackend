package com.eliteshop.colombia.product.domain.model;

import java.util.Objects;
import lombok.Getter;

@Getter
public class ProductCategory {
  private final String value;

  public ProductCategory(String value) {
    Objects.requireNonNull(value, "La categoría del producto no puede ser null");
    if (value.isBlank()) {
      throw new IllegalArgumentException("La categoría del producto no puede estar vacía");
    }
    if (value.length() > 100) {
      throw new IllegalArgumentException(
          "La categoría del producto no puede exceder los 100 caracteres");
    }
    this.value = value;
  }
}
