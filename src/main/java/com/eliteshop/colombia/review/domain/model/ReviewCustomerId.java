package com.eliteshop.colombia.review.domain.model;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class ReviewCustomerId {
  private final UUID value;

  public ReviewCustomerId(UUID value) {
    Objects.requireNonNull(value, "El ID del cliente no puede ser nulo");
    this.value = value;
  }
}
