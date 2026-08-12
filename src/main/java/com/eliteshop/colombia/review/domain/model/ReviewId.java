package com.eliteshop.colombia.review.domain.model;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class ReviewId {
  private final UUID value;

  public ReviewId(UUID value) {
    Objects.requireNonNull(value, "El ID de la reseña no puede ser nulo");
    this.value = value;
  }

  public static ReviewId generate() {
    return new ReviewId(UUID.randomUUID());
  }
}
