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

    // Unicidad del nombre: antes no se comprobaba en el update, así que renombrar un
    // producto al nombre de otro violaba la constraint y devolvía 500.
    repository
        .findByName(product.getName())
        .filter(existing -> !existing.getId().equals(product.getId()))
        .ifPresent(
            existing -> {
              throw new com.eliteshop.colombia.product.domain.exception
                  .ProductAlreadyExistsException(
                  "Ya existe un producto con el nombre " + product.getName().getValue());
            });

    repository.update(product);
    log.info("Producto actualizado exitosamente con id: {}", product.getId());
  }
}
