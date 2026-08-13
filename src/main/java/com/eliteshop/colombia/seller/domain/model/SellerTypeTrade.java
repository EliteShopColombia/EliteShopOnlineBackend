package com.eliteshop.colombia.seller.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SellerTypeTrade {
  NATURAL("Natural Person"),
  LEGAL("Legal Person");

  private final String value;
}
