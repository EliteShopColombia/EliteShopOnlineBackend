package com.eliteshop.colombia.product.domain.repository;

import com.eliteshop.colombia.product.domain.model.ProductId;
import com.eliteshop.colombia.product.domain.model.ProductImage;
import java.util.List;

public interface ProductImageRepository {
  List<ProductImage> findByProductId(ProductId productId);

  ProductImage save(ProductImage image);

  void deleteByProductId(ProductId productId);
}
