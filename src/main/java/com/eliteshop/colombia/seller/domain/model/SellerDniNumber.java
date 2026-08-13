package com.eliteshop.colombia.seller.domain.model;

import com.eliteshop.colombia.seller.domain.exception.SellerInvalidDniNumberException;
import java.util.Objects;
import java.util.regex.Pattern;
import lombok.Getter;

@Getter
public class SellerDniNumber {
  private static final Pattern CC_PATTERN = Pattern.compile("^[0-9]{6,10}$");
  private static final Pattern CE_PATTERN = Pattern.compile("^[A-Za-z0-9]{5,20}$");
  private static final Pattern PS_PATTERN = Pattern.compile("^[A-Za-z0-9]{5,20}$");
  private static final Pattern NIT_PATTERN = Pattern.compile("^[0-9]{9,11}$");

  private final String value;

  public SellerDniNumber(String value) {
    Objects.requireNonNull(value, "El número de documento no puede ser nulo");
    String trimmed = value.trim();
    if (trimmed.isEmpty()) {
      throw new SellerInvalidDniNumberException("El número de documento no puede estar vacío");
    }
    this.value = trimmed;
  }

  public boolean isValidForType(SellerTypeDni type) {
    return switch (type) {
      case CC -> CC_PATTERN.matcher(value).matches();
      case CE -> CE_PATTERN.matcher(value).matches();
      case PS -> PS_PATTERN.matcher(value).matches();
      case NIT -> NIT_PATTERN.matcher(value).matches();
    };
  }
}
