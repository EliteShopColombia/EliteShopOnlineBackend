package com.eliteshop.colombia.seller.domain.model.verification;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class SellerVerificationSellerId {
  private final UUID value;

  public SellerVerificationSellerId(UUID value) {
    Objects.requireNonNull(value, "El ID del vendedor no puede ser nulo");
    this.value = value;
  }
}
