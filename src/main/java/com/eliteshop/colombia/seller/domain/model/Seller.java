package com.eliteshop.colombia.seller.domain.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class Seller {
  @NonNull private final SellerId id;
  @NonNull private final SellerTypeTrade typeTrade;
  @NonNull private final SellerTypeDni typeDni;
  @NonNull private final SellerDniNumber dniNumber;
  @NonNull private final SellerTradeName tradeName;
  @NonNull private final SellerFullname fullname;
  @NonNull private final SellerIsActive isActive;
  @NonNull private final SellerIsVerified isVerified;
  @NonNull private final SellerCreatedAt createdAt;
  private final SellerUpdatedAt updatedAt;
  private final SellerContact contact;
  private final SellerBankInfo bankInfo;
}
