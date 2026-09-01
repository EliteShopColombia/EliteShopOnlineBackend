package com.eliteshop.colombia.product.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ProductImage {
  private final ProductImageId id;
  private final ProductId productId;
  private final ProductImageUrl imageUrl;
  private final ProductImageOrder order;
}
