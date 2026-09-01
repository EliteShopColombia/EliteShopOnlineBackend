package com.eliteshop.colombia.customer.domain.model;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class CustomerId {
  private final UUID value;

  public CustomerId(UUID value) {
    this.value = Objects.requireNonNull(value, "Customer ID no puede ser null");
  }
}
