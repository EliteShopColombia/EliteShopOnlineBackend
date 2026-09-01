package com.eliteshop.colombia.customer.domain.model;

import java.util.Objects;
import java.util.regex.Pattern;
import lombok.Getter;

@Getter
public class CustomerPhoneNumber {
  private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10}$");

  private final String value;

  public CustomerPhoneNumber(String value) {
    Objects.requireNonNull(value, "Numero de telefono no puede ser null");
    if (value.isBlank()) {
      throw new IllegalArgumentException("Numero de telefono no puede estar vacio");
    }
    String digits = value.replaceAll("[^0-9]", "");
    if (!PHONE_PATTERN.matcher(digits).matches()) {
      throw new IllegalArgumentException(
          "Numero de telefono debe tener exactamente 10 digitos: " + value);
    }
    this.value = digits;
  }
}
