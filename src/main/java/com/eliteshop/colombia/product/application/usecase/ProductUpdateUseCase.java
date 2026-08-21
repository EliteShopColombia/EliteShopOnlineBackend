package com.eliteshop.colombia.product.application.usecase;

import com.eliteshop.colombia.product.domain.exception.ProductNotFoundException;
import com.eliteshop.colombia.product.domain.model.Product;
import com.eliteshop.colombia.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ProductUpdateUseCase {

  private final ProductRepository repository;

  public void execute(Product product) {
    log.info("Actualizando producto con id: {}", product.getId());
    if (product.getId() == null || repository.findById(product.getId()).isEmpty()) {
      throw new ProductNotFoundException("El producto no existe en la plataforma");
    }

    repository.update(product);
    log.info("Producto actualizado exitosamente con id: {}", product.getId());
  }
}
