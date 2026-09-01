package com.eliteshop.colombia.payment.domain.model.paymentmethod;

import java.util.Objects;
import lombok.Getter;

@Getter
public class CustomerPaymentMethodLast4 {
  private final String value;

  public CustomerPaymentMethodLast4(String value) {
    Objects.requireNonNull(value, "Los últimos 4 dígitos no pueden ser nulos");
    if (value.length() != 4) {
      throw new IllegalArgumentException("Los últimos 4 dígitos deben tener 4 caracteres");
    }
    this.value = value;
  }
}
