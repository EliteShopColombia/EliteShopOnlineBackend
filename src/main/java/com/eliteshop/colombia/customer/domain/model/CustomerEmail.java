package com.eliteshop.colombia.customer.domain.model;

import java.util.Objects;
import java.util.regex.Pattern;
import lombok.Getter;

@Getter
public class CustomerEmail {
  private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

  private final String value;

  public CustomerEmail(String value) {
    Objects.requireNonNull(value, "Email no puede ser null");
    if (value.isBlank()) {
      throw new IllegalArgumentException("El email no puede estar vacío");
    }
    if (!EMAIL_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException("El email no tiene un formato válido: " + value);
    }
    this.value = value;
  }
}
