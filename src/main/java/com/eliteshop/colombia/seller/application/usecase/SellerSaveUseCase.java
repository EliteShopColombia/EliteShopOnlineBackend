package com.eliteshop.colombia.seller.application.usecase;

import com.eliteshop.colombia.seller.domain.exception.SellerAlreadyExistsException;
import com.eliteshop.colombia.seller.domain.model.Seller;
import com.eliteshop.colombia.seller.domain.repository.SellerRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SellerSaveUseCase {

  private final SellerRepository repository;

  public void execute(Seller seller) {
    repository
        .findByDniNumber(seller.getDniNumber())
        .ifPresent(
            existingSeller -> {
              throw new SellerAlreadyExistsException(
                  "Ya existe un vendedor con el documento " + seller.getDniNumber().getValue());
            });

    repository.save(seller);
  }
}
