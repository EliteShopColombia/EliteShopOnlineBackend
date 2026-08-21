package com.eliteshop.colombia.payment.domain.model.paymentmethod;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TokenizedCard {
  private final String token;
  private final String last4;
  private final String brand;
  private final int expiryMonth;
  private final int expiryYear;
}
