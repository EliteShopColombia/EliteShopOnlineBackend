package com.eliteshop.colombia.seller.domain.model;

import com.eliteshop.colombia.seller.domain.exception.SellerInvalidEmailException;
import java.util.Objects;
import java.util.regex.Pattern;
import lombok.Getter;

@Getter
public class SellerEmail {
  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
  private static final int MAX_LENGTH = 150;

  private final String value;

  public SellerEmail(String value) {
    Objects.requireNonNull(value, "El email no puede ser nulo");
    String trimmed = value.trim();
    if (trimmed.isEmpty()) {
      throw new SellerInvalidEmailException("El email no puede estar vacío");
    }
    if (trimmed.length() > MAX_LENGTH) {
      throw new SellerInvalidEmailException(
          "El email no puede exceder " + MAX_LENGTH + " caracteres");
    }
    if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
      throw new SellerInvalidEmailException("El formato del email no es válido");
    }
    this.value = trimmed.toLowerCase();
  }
}
