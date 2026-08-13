package com.eliteshop.colombia.seller.application.usecase;

import com.eliteshop.colombia.seller.domain.model.Seller;
import com.eliteshop.colombia.seller.domain.model.SellerId;
import com.eliteshop.colombia.seller.domain.repository.SellerRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SellerFindByIdUseCase {

  private final SellerRepository repository;

  public Optional<Seller> execute(SellerId id) {
    if (id == null) return Optional.empty();

    return repository.findById(id);
  }
}
