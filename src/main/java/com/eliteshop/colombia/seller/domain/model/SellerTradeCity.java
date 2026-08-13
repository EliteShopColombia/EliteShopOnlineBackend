package com.eliteshop.colombia.seller.domain.model;

import com.eliteshop.colombia.seller.domain.exception.SellerInvalidTradeCityException;
import java.util.Objects;
import lombok.Getter;

@Getter
public class SellerTradeCity {
  private static final int MIN_LENGTH = 2;
  private static final int MAX_LENGTH = 60;

  private final String value;

  public SellerTradeCity(String value) {
    Objects.requireNonNull(value, "La ciudad no puede ser nula");
    String trimmed = value.trim();
    if (trimmed.length() < MIN_LENGTH) {
      throw new SellerInvalidTradeCityException(
          "La ciudad debe tener al menos " + MIN_LENGTH + " caracteres");
    }
    if (trimmed.length() > MAX_LENGTH) {
      throw new SellerInvalidTradeCityException(
          "La ciudad no puede exceder " + MAX_LENGTH + " caracteres");
    }
    this.value = trimmed;
  }
}
