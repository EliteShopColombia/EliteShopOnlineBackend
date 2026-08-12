package com.eliteshop.colombia.seller.domain.model;

import java.util.Objects;
import lombok.Getter;

@Getter
public class SellerIsActive {
  private final Boolean value;

  public SellerIsActive(Boolean value) {
    Objects.requireNonNull(value, "El estado de actividad no puede ser nulo");
    this.value = value;
  }
}
