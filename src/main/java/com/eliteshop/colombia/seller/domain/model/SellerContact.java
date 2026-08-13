package com.eliteshop.colombia.seller.domain.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class SellerContact {
  @NonNull private final SellerEmail email;
  @NonNull private final SellerPhoneNumber phoneNumber;
  @NonNull private final SellerTradeAddress tradeAddress;
  @NonNull private final SellerTradeDepartment tradeDepartment;
  @NonNull private final SellerTradeCity tradeCity;
}
