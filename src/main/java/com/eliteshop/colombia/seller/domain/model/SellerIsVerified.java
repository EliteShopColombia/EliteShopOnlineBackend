package com.eliteshop.colombia.seller.domain.model;

import java.util.Objects;
import lombok.Getter;

@Getter
public class SellerIsVerified {
  private final Boolean value;

  public SellerIsVerified(Boolean value) {
    Objects.requireNonNull(value, "El estado de verificacion no puede ser nulo");
    this.value = value;
  }
}
