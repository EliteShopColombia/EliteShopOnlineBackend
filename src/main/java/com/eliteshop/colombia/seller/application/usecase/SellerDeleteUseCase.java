package com.eliteshop.colombia.seller.application.usecase;

import com.eliteshop.colombia.seller.domain.exception.SellerNotFoundException;
import com.eliteshop.colombia.seller.domain.model.SellerId;
import com.eliteshop.colombia.seller.domain.repository.SellerRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SellerDeleteUseCase {

  private final SellerRepository repository;

  public void execute(SellerId id) {
    if (repository.findById(id).isEmpty()) {
      throw new SellerNotFoundException("El vendedor no existe en la plataforma");
    }

    repository.delete(id);
  }
}
