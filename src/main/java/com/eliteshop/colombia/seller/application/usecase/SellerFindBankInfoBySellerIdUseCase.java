package com.eliteshop.colombia.seller.application.usecase;

import com.eliteshop.colombia.seller.domain.model.Seller;
import com.eliteshop.colombia.seller.domain.model.SellerId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SellerFindBankInfoBySellerIdUseCase {

  private final SellerFindByIdUseCase findByIdUseCase;

  public Optional<Seller> execute(SellerId sellerId) {
    if (sellerId == null) return Optional.empty();

    return findByIdUseCase.execute(sellerId).filter(seller -> seller.getBankInfo() != null);
  }
}
