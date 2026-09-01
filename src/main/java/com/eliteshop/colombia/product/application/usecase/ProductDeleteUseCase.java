package com.eliteshop.colombia.product.application.usecase;

import com.eliteshop.colombia.product.domain.exception.ProductNotFoundException;
import com.eliteshop.colombia.product.domain.model.ProductId;
import com.eliteshop.colombia.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ProductDeleteUseCase {

  private final ProductRepository repository;

  public void execute(ProductId id) {
    log.info("Iniciando eliminación de producto con id: {}", id);
    if (repository.findById(id).isEmpty()) {
      throw new ProductNotFoundException("El producto no existe en la plataforma");
    }

    repository.delete(id);
    log.info("Producto eliminado exitosamente con id: {}", id);
  }
}
