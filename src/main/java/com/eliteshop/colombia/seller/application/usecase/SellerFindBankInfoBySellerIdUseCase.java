package com.eliteshop.colombia.seller.application.usecase;

import com.eliteshop.colombia.seller.domain.model.Seller;
import com.eliteshop.colombia.seller.domain.model.SellerId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SellerFindBankInfoBySellerIdUseCase {

  private final SellerFindByIdUseCase findByIdUseCase;

  public Optional<Seller> execute(SellerId sellerId) {
    log.info("Buscando información bancaria del vendedor sellerId={}", sellerId);
    if (sellerId == null) return Optional.empty();

    Optional<Seller> seller =
        findByIdUseCase.execute(sellerId).filter(s -> s.getBankInfo() != null);
    seller.ifPresent(s -> log.info("Información bancaria encontrada para sellerId={}", sellerId));
    if (seller.isEmpty())
      log.info("No se encontró información bancaria para sellerId={}", sellerId);
    return seller;
  }
}
