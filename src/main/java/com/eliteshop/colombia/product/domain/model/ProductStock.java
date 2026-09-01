package com.eliteshop.colombia.product.domain.model;

import lombok.Getter;

@Getter
public class ProductStock {
  private final int value;

  public ProductStock(int value) {
    if (value < 0) {
      throw new IllegalArgumentException("Stock no puede ser negativo: " + value);
    }
    this.value = value;
  }
}
