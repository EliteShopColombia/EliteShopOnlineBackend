package com.eliteshop.colombia.payment.domain.model.paymentmethod;

import java.util.Objects;
import lombok.Getter;

@Getter
public class CustomerPaymentMethodBrand {
  private final String value;

  public CustomerPaymentMethodBrand(String value) {
    Objects.requireNonNull(value, "La franquicia no puede ser nula");
    if (value.isBlank()) {
      throw new IllegalArgumentException("La franquicia no puede estar vacía");
    }
    this.value = value;
  }
}
