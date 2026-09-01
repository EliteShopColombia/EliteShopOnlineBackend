package com.eliteshop.colombia.product.application.usecase;

import com.eliteshop.colombia.product.domain.model.Product;
import com.eliteshop.colombia.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ProductSaveUseCase {

  private final ProductRepository repository;

  public void execute(Product product) {
    log.info("Guardando producto con nombre: {}", product.getName().getValue());
    repository
        .findByName(product.getName())
        .ifPresent(
            existingProduct -> {
              throw new com.eliteshop.colombia.product.domain.exception
                  .ProductAlreadyExistsException(
                  "Ya existe un producto con el nombre " + product.getName().getValue());
            });

    repository.save(product);
    log.info("Producto guardado exitosamente: {}", product.getName().getValue());
  }
}
