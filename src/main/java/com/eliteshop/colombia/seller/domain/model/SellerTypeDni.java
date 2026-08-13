package com.eliteshop.colombia.seller.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SellerTypeDni {
  CC("Cédula de Ciudadania"),
  CE("Cédula de Extranjeria"),
  PS("Pasaporte"),
  NIT("Identificación Tributaria");

  private final String value;
}
