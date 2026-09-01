package com.eliteshop.colombia.order.domain.model;

import java.util.Objects;
import lombok.Getter;

@Getter
public class OrderShippingAddress {
  private final String value;

  public OrderShippingAddress(String value) {
    Objects.requireNonNull(value, "Direccion de envio no puede ser null");
    if (value.isBlank()) {
      throw new IllegalArgumentException("Direccion de envio no puede estar vacia");
    }
    this.value = value;
  }
}
