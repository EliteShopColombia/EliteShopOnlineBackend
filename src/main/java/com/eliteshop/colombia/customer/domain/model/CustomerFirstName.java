package com.eliteshop.colombia.customer.domain.model;

import java.util.Objects;
import lombok.Getter;

@Getter
public class CustomerFirstName {
  private final String value;

  public CustomerFirstName(String value) {
    Objects.requireNonNull(value, "Nombre no puede ser null");
    if (value.isBlank()) {
      throw new IllegalArgumentException("Nombre no puede estar vacio");
    }
    this.value = value;
  }
}
