package com.eliteshop.colombia.product.domain.model;

import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ProductImageId {
  private final UUID value;

  public static ProductImageId generate() {
    return new ProductImageId(UUID.randomUUID());
  }
}
