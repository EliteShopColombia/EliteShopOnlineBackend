package com.eliteshop.colombia.seller.domain.model.verification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Score de detección anti-spoofing (0.0 - 1.0). */
@RequiredArgsConstructor
@Getter
public class SellerVerificationAntispoofScore {
  private final Double value;
}
