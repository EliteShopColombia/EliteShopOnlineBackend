package com.eliteshop.colombia.payment.domain.model.paymentmethod;

import java.util.Objects;
import lombok.Getter;

@Getter
public class CustomerPaymentMethodEpaycoToken {
  private final String value;

  public CustomerPaymentMethodEpaycoToken(String value) {
    Objects.requireNonNull(value, "El token de ePayco no puede ser nulo");
    if (value.isBlank()) {
      throw new IllegalArgumentException("El token de ePayco no puede estar vacío");
    }
    this.value = value;
  }
}
