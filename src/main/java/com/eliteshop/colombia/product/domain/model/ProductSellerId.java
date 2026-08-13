package com.eliteshop.colombia.product.domain.model;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class ProductSellerId {
  private final UUID value;

  public ProductSellerId(UUID value) {
    Objects.requireNonNull(value, "El ID del vendedor no puede ser nulo");
    this.value = value;
  }
}
