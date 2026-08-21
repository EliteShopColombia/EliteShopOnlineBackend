package com.eliteshop.colombia.seller.application.usecase;

import com.eliteshop.colombia.seller.domain.model.Seller;
import com.eliteshop.colombia.seller.domain.model.SellerId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SellerFindContactBySellerIdUseCase {

  private final SellerFindByIdUseCase findByIdUseCase;

  public Optional<Seller> execute(SellerId sellerId) {
    log.info("Buscando contacto del vendedor sellerId={}", sellerId);
    if (sellerId == null) return Optional.empty();

    Optional<Seller> seller = findByIdUseCase.execute(sellerId).filter(s -> s.getContact() != null);
    seller.ifPresent(s -> log.info("Contacto encontrado para sellerId={}", sellerId));
    if (seller.isEmpty()) log.info("No se encontro contacto para sellerId={}", sellerId);
    return seller;
  }
}
