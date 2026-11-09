package com.eliteshop.colombia.customer.domain.model;

import java.util.Objects;
import lombok.Getter;

@Getter
public class CustomerLastName {
  private final String value;

  public CustomerLastName(String value) {
    Objects.requireNonNull(value, "El apellido no puede ser nulo");
    if (value.isBlank()) {
      throw new IllegalArgumentException("El apellido no puede estar vacío");
    }
    this.value = value;
  }
}
