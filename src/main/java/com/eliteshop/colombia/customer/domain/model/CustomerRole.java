package com.eliteshop.colombia.customer.domain.model;

import java.util.Objects;
import lombok.Getter;

@Getter
public class CustomerRole {
  private final String value;

  public CustomerRole(String value) {
    Objects.requireNonNull(value, "El rol no puede ser nulo");
    this.value = value;
  }
}
