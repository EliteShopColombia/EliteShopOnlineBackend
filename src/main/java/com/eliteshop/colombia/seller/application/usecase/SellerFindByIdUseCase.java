package com.eliteshop.colombia.seller.application.usecase;

import com.eliteshop.colombia.seller.domain.model.Seller;
import com.eliteshop.colombia.seller.domain.model.SellerId;
import com.eliteshop.colombia.seller.domain.repository.SellerRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SellerFindByIdUseCase {

  private final SellerRepository repository;

  public Optional<Seller> execute(SellerId id) {
    log.info("Buscando vendedor por id={}", id);
    if (id == null) return Optional.empty();

    Optional<Seller> seller = repository.findById(id);
    seller.ifPresent(s -> log.info("Vendedor encontrado con id={}", id));
    if (seller.isEmpty()) log.info("No se encontro vendedor con id={}", id);
    return seller;
  }
}
