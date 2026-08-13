package com.eliteshop.colombia.seller.domain.model;

import com.eliteshop.colombia.seller.domain.exception.SellerInvalidBankNameException;
import java.util.List;
import java.util.Objects;
import lombok.Getter;

@Getter
public class SellerBankName {
  private static final List<String> VALID_BANKS =
      List.of(
          "Bancolombia",
          "Banco de Bogotá",
          "Banco AV Villas",
          "Banco de Occidente",
          "Banco Popular",
          "Banco Santander",
          "Banco BBVA Colombia",
          "Banco Itaú",
          "Banco Falabella",
          "Banco Pichincha",
          "Banco Coomeva",
          "Banco Agrario",
          "Banco Davivienda",
          "Banco Colpatria",
          "Scotiabank Colpatria",
          "Banco Caja Social",
          "Banco Brightmar",
          "Banco Interfilial",
          "Nequi",
          "Daviplata",
          "RappiPay");
  private static final int MAX_LENGTH = 150;

  private final String value;

  public SellerBankName(String value) {
    Objects.requireNonNull(value, "El nombre del banco no puede ser nulo");
    String trimmed = value.trim();
    if (trimmed.isEmpty()) {
      throw new SellerInvalidBankNameException("El nombre del banco no puede estar vacío");
    }
    if (trimmed.length() > MAX_LENGTH) {
      throw new SellerInvalidBankNameException(
          "El nombre del banco no puede exceder " + MAX_LENGTH + " caracteres");
    }
    boolean exists = VALID_BANKS.stream().anyMatch(b -> b.equalsIgnoreCase(trimmed));
    if (!exists) {
      throw new SellerInvalidBankNameException("El banco no es válido para Colombia");
    }
    this.value = trimmed;
  }
}
