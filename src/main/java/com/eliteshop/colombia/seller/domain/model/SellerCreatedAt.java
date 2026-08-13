package com.eliteshop.colombia.seller.domain.model;

import java.sql.Timestamp;
import java.util.Objects;
import lombok.Getter;

@Getter
public class SellerCreatedAt {
  private final Timestamp value;

  public SellerCreatedAt(Timestamp value) {
    Objects.requireNonNull(value, "La fecha de creación no puede ser nula");
    this.value = value;
  }
}
