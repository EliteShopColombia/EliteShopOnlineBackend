package com.eliteshop.colombia.seller.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SellerTypeBankAccount {
  SAVINGS("Ahorros"),
  CHECKING("Corriente"),
  WALLET("Digital");

  private final String value;
}
