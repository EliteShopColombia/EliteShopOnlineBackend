package com.eliteshop.colombia.seller.application.usecase;

import com.eliteshop.colombia.seller.domain.exception.SellerNotFoundException;
import com.eliteshop.colombia.seller.domain.model.Seller;
import com.eliteshop.colombia.seller.domain.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SellerUpdateUseCase {

  private final SellerRepository repository;

  public void execute(Seller seller) {
    log.info("Actualizando vendedor con id={}", seller.getId());
    if (seller.getId() == null || repository.findById(seller.getId()).isEmpty()) {
      log.error("No se encontró el vendedor con id={}", seller.getId());
      throw new SellerNotFoundException("El vendedor no existe en la plataforma");
    }

    repository.update(seller);
    log.info("Vendedor actualizado exitosamente con id={}", seller.getId());
  }
}
