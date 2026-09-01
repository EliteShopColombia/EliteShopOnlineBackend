package com.eliteshop.colombia.product.domain.repository;

import com.eliteshop.colombia.product.domain.model.Product;
import com.eliteshop.colombia.product.domain.model.ProductId;
import com.eliteshop.colombia.product.domain.model.ProductName;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepository {
  Product save(Product product);

  void update(Product product);

  void delete(ProductId id);

  List<Product> findAll();

  Page<Product> findAll(Pageable pageable);

  Optional<Product> findById(ProductId id);

  Optional<Product> findByName(ProductName name);

  void reduceStock(ProductId id, int quantity);

  void restoreStock(ProductId id, int quantity);
}
