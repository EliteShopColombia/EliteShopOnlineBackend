package com.eliteshop.colombia.payment.domain.model.paymentmethod;

import java.util.Objects;
import lombok.Getter;

@Getter
public class CustomerPaymentMethodExpiryYear {
  private final Integer value;

  public CustomerPaymentMethodExpiryYear(Integer value) {
    Objects.requireNonNull(value, "El año de vencimiento no puede ser nulo");
    if (value < 2020) {
      throw new IllegalArgumentException("El año de vencimiento no es válido");
    }
    this.value = value;
  }
}
