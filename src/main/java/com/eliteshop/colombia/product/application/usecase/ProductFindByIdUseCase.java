package com.eliteshop.colombia.product.application.usecase;

import com.eliteshop.colombia.product.domain.model.Product;
import com.eliteshop.colombia.product.domain.model.ProductId;
import com.eliteshop.colombia.product.domain.repository.ProductRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProductFindByIdUseCase {

  private final ProductRepository repository;

  public Optional<Product> execute(ProductId id) {
    if (id == null) return Optional.empty();

    return repository.findById(id);
  }
}
