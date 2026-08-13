package com.eliteshop.colombia.seller.domain.model;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class SellerId {
  private final UUID value;

  public SellerId(UUID value) {
    Objects.requireNonNull(value, "El ID del vendedor no puede ser nulo");
    this.value = value;
  }

  public static SellerId generate() {
    return new SellerId(UUID.randomUUID());
  }
}
