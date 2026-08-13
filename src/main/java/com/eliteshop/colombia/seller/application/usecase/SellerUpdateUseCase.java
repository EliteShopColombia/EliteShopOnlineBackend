package com.eliteshop.colombia.seller.application.usecase;

import com.eliteshop.colombia.seller.domain.exception.SellerNotFoundException;
import com.eliteshop.colombia.seller.domain.model.Seller;
import com.eliteshop.colombia.seller.domain.repository.SellerRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SellerUpdateUseCase {

  private final SellerRepository repository;

  public void execute(Seller seller) {
    if (seller.getId() == null || repository.findById(seller.getId()).isEmpty()) {
      throw new SellerNotFoundException("El vendedor no existe en la plataforma");
    }

    repository.update(seller);
  }
}
