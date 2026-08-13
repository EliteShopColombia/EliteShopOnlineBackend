package com.eliteshop.colombia.seller.application.usecase;

import com.eliteshop.colombia.seller.domain.model.Seller;
import com.eliteshop.colombia.seller.domain.repository.SellerRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SellerFindAllUseCase {

  private final SellerRepository repository;

  public List<Seller> execute() {
    return repository.findAll();
  }
}
