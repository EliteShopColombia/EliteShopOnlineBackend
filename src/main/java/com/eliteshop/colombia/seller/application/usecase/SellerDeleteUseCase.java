package com.eliteshop.colombia.seller.application.usecase;

import com.eliteshop.colombia.seller.domain.exception.SellerNotFoundException;
import com.eliteshop.colombia.seller.domain.model.SellerId;
import com.eliteshop.colombia.seller.domain.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SellerDeleteUseCase {

  private final SellerRepository repository;

  public void execute(SellerId id) {
    log.info("Intentando eliminar vendedor con id={}", id);
    if (repository.findById(id).isEmpty()) {
      log.error("No se encontro el vendedor con id={}", id);
      throw new SellerNotFoundException("El vendedor no existe en la plataforma");
    }

    repository.delete(id);
    log.info("Vendedor eliminado exitosamente con id={}", id);
  }
}
