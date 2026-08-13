package com.eliteshop.colombia.seller.domain.model;

import com.eliteshop.colombia.seller.domain.exception.SellerInvalidTradeAddressException;
import java.util.Objects;
import lombok.Getter;

@Getter
public class SellerTradeAddress {
  private static final int MIN_LENGTH = 5;
  private static final int MAX_LENGTH = 150;

  private final String value;

  public SellerTradeAddress(String value) {
    Objects.requireNonNull(value, "La dirección no puede ser nula");
    String trimmed = value.trim();
    if (trimmed.length() < MIN_LENGTH) {
      throw new SellerInvalidTradeAddressException(
          "La dirección debe tener al menos " + MIN_LENGTH + " caracteres");
    }
    if (trimmed.length() > MAX_LENGTH) {
      throw new SellerInvalidTradeAddressException(
          "La dirección no puede exceder " + MAX_LENGTH + " caracteres");
    }
    this.value = trimmed;
  }
}
