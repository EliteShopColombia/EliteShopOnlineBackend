package com.eliteshop.colombia.product.application.usecase;

import com.eliteshop.colombia.product.domain.model.Product;
import com.eliteshop.colombia.product.domain.model.ProductId;
import com.eliteshop.colombia.product.domain.repository.ProductRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ProductFindByIdUseCase {

  private final ProductRepository repository;

  public Optional<Product> execute(ProductId id) {
    log.info("Buscando producto con id: {}", id);
    if (id == null) return Optional.empty();

    Optional<Product> product = repository.findById(id);
    log.info("Producto encontrado: {}", product.isPresent());
    return product;
  }
}
