package com.eliteshop.colombia.seller.domain.model;

import com.eliteshop.colombia.seller.domain.exception.SellerInvalidTradeNameException;
import java.util.Objects;
import lombok.Getter;

@Getter
public class SellerTradeName {
  private static final int MIN_LENGTH = 2;
  private static final int MAX_LENGTH = 100;

  private final String value;

  public SellerTradeName(String value) {
    Objects.requireNonNull(value, "El nombre comercial no puede ser nulo");
    String trimmed = value.trim();
    if (trimmed.length() < MIN_LENGTH) {
      throw new SellerInvalidTradeNameException(
          "El nombre comercial debe tener al menos " + MIN_LENGTH + " caracteres");
    }
    if (trimmed.length() > MAX_LENGTH) {
      throw new SellerInvalidTradeNameException(
          "El nombre comercial no puede exceder " + MAX_LENGTH + " caracteres");
    }
    this.value = trimmed;
  }
}
