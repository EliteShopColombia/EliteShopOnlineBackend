package com.eliteshop.colombia.seller.domain.model.verification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Score de confianza del liveness detection (0.0 - 1.0). */
@RequiredArgsConstructor
@Getter
public class SellerVerificationLivenessConfidence {
  private final Double value;
}
