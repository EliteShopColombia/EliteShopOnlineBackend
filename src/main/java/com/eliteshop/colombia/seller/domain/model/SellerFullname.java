package com.eliteshop.colombia.seller.domain.model;

import com.eliteshop.colombia.seller.domain.exception.SellerInvalidFullnameException;
import java.util.Objects;
import java.util.regex.Pattern;
import lombok.Getter;

@Getter
public class SellerFullname {
  private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-záéíóúñÁÉÍÓÚÑÜü\\s]+$");
  private static final int MIN_LENGTH = 2;
  private static final int MAX_LENGTH = 100;

  private final String value;

  public SellerFullname(String value) {
    Objects.requireNonNull(value, "El nombre completo no puede ser nulo");
    String trimmed = value.trim();
    if (trimmed.length() < MIN_LENGTH) {
      throw new SellerInvalidFullnameException(
          "El nombre completo debe tener al menos " + MIN_LENGTH + " caracteres");
    }
    if (trimmed.length() > MAX_LENGTH) {
      throw new SellerInvalidFullnameException(
          "El nombre completo no puede exceder " + MAX_LENGTH + " caracteres");
    }
    if (!NAME_PATTERN.matcher(trimmed).matches()) {
      throw new SellerInvalidFullnameException(
          "El nombre completo solo puede contener letras y espacios");
    }
    this.value = trimmed;
  }
}
