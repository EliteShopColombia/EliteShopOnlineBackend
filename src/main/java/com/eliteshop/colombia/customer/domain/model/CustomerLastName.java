package com.eliteshop.colombia.customer.domain.model;

import java.util.Objects;
import lombok.Getter;

@Getter
public class CustomerLastName {
  private final String value;

  public CustomerLastName(String value) {
    Objects.requireNonNull(value, "Apellido no puede ser null");
    if (value.isBlank()) {
      throw new IllegalArgumentException("Apellido no puede estar vacio");
    }
    this.value = value;
  }
}
