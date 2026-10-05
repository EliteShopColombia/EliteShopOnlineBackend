package com.eliteshop.colombia.seller.domain.model.verification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Nombre completo extraído por OCR del documento. */
@RequiredArgsConstructor
@Getter
public class SellerVerificationOcrCedulaName {
  private final String value;
}
