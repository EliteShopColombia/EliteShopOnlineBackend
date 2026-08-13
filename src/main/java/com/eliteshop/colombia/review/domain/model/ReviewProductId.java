package com.eliteshop.colombia.review.domain.model;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class ReviewProductId {
  private final UUID value;

  public ReviewProductId(UUID value) {
    Objects.requireNonNull(value, "El ID del producto no puede ser nulo");
    this.value = value;
  }
}
