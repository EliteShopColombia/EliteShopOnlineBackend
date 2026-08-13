package com.eliteshop.colombia.product.domain.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class Product {
  @NonNull private final ProductId id;
  @NonNull private final ProductSellerId sellerId;
  @NonNull private final ProductName name;
  @NonNull private final ProductPrice price;
  @NonNull private final ProductStock stock;
}
