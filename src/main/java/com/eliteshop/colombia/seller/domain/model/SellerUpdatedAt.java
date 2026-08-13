package com.eliteshop.colombia.seller.domain.model;

import java.sql.Timestamp;
import java.util.Objects;
import lombok.Getter;

@Getter
public class SellerUpdatedAt {
  private final Timestamp value;

  public SellerUpdatedAt(Timestamp value) {
    Objects.requireNonNull(value, "La fecha de actualización no puede ser nula");
    this.value = value;
  }
}
