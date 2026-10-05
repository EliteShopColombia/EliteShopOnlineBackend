package com.eliteshop.colombia.seller.domain.model.verification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Número de cédula extraído por OCR del documento. */
@RequiredArgsConstructor
@Getter
public class SellerVerificationOcrCedulaNumber {
  private final String value;
}
