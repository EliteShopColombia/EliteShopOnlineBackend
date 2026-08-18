package com.eliteshop.colombia.product.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
  private final List<ProductImage> images;

  public Product(
      ProductId id,
      ProductSellerId sellerId,
      ProductName name,
      ProductPrice price,
      ProductStock stock) {
    this(id, sellerId, name, price, stock, new ArrayList<>());
  }

  public List<ProductImage> getImages() {
    return images != null ? Collections.unmodifiableList(images) : Collections.emptyList();
  }
}
