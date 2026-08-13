package com.eliteshop.colombia.seller.domain.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class SellerBankInfo {
  @NonNull private final SellerBankName bankName;
  @NonNull private final SellerTypeBankAccount typeBankAccount;
  @NonNull private final SellerNumberAccount numberAccount;
}
