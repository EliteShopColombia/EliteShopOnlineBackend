package com.eliteshop.colombia.payment.domain.model.paymentmethod;

import java.util.Objects;
import lombok.Getter;

@Getter
public class CustomerPaymentMethodEpaycoCustomerId {
  private final String value;

  public CustomerPaymentMethodEpaycoCustomerId(String value) {
    Objects.requireNonNull(value, "El customerId de ePayco no puede ser nulo");
    if (value.isBlank()) {
      throw new IllegalArgumentException("El customerId de ePayco no puede estar vacío");
    }
    this.value = value;
  }
}
