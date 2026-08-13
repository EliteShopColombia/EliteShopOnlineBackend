package com.eliteshop.colombia.seller.application.usecase;

import com.eliteshop.colombia.seller.domain.model.Seller;
import com.eliteshop.colombia.seller.domain.model.SellerDniNumber;
import com.eliteshop.colombia.seller.domain.repository.SellerRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SellerFindByDniUseCase {

  private final SellerRepository repository;

  public Optional<Seller> execute(SellerDniNumber dniNumber) {
    if (dniNumber == null) return Optional.empty();

    return repository.findByDniNumber(dniNumber);
  }
}
