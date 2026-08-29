package com.eliteshop.colombia.seller.application.usecase;

import com.eliteshop.colombia.seller.domain.model.Seller;
import com.eliteshop.colombia.seller.domain.model.SellerDniNumber;
import com.eliteshop.colombia.seller.domain.repository.SellerRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SellerFindByDniUseCase {

  private final SellerRepository repository;

  public Optional<Seller> execute(SellerDniNumber dniNumber) {
    log.info("Buscando vendedor por dni={}", dniNumber);
    if (dniNumber == null) return Optional.empty();

    Optional<Seller> seller = repository.findByDniNumber(dniNumber);
    seller.ifPresent(s -> log.info("Vendedor encontrado con dni={}", dniNumber));
    if (seller.isEmpty()) log.info("No se encontró vendedor con dni={}", dniNumber);
    return seller;
  }
}
