package com.eliteshop.colombia.customer.domain.model;

import java.util.Objects;
import lombok.Getter;

@Getter
public class CustomerFirstName {
  private final String value;

  public CustomerFirstName(String value) {
    Objects.requireNonNull(value, "El nombre no puede ser nulo");
    if (value.isBlank()) {
      throw new IllegalArgumentException("El nombre no puede estar vacío");
    }
    this.value = value;
  }
}
