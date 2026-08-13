package com.eliteshop.colombia.seller.domain.model;

import com.eliteshop.colombia.seller.domain.exception.SellerInvalidPhoneNumberException;
import java.util.Objects;
import java.util.regex.Pattern;
import lombok.Getter;

@Getter
public class SellerPhoneNumber {
  private static final Pattern PHONE_PATTERN = Pattern.compile("^(3[0-9]{9}|[1-9][0-9]{6,9})$");
  private static final int EXPECTED_LENGTH = 10;

  private final String value;

  public SellerPhoneNumber(String value) {
    Objects.requireNonNull(value, "El teléfono no puede ser nulo");
    String cleaned = value.replaceAll("[\\s\\-\\(\\)\\+]", "");
    if (cleaned.isEmpty()) {
      throw new SellerInvalidPhoneNumberException("El teléfono no puede estar vacío");
    }
    if (!cleaned.chars().allMatch(Character::isDigit)) {
      throw new SellerInvalidPhoneNumberException("El teléfono solo puede contener dígitos");
    }
    if (cleaned.length() != EXPECTED_LENGTH) {
      throw new SellerInvalidPhoneNumberException(
          "El teléfono debe tener exactamente " + EXPECTED_LENGTH + " dígitos");
    }
    if (!PHONE_PATTERN.matcher(cleaned).matches()) {
      throw new SellerInvalidPhoneNumberException(
          "El teléfono no es válido para Colombia (debe iniciar con 3 para celular o 1-9 para fijo)");
    }
    this.value = cleaned;
  }
}
