package com.eliteshop.colombia.product.domain.model;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class ProductId {
  private final UUID value;

  public ProductId(UUID value) {
    Objects.requireNonNull(value, "El ID del producto no puede ser nulo");
    this.value = value;
  }

  public static ProductId generate() {
    return new ProductId(UUID.randomUUID());
  }
}
