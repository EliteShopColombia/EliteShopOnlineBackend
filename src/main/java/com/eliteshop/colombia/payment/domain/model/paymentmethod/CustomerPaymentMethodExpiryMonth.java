package com.eliteshop.colombia.payment.domain.model.paymentmethod;

import java.util.Objects;
import lombok.Getter;

@Getter
public class CustomerPaymentMethodExpiryMonth {
  private final Integer value;

  public CustomerPaymentMethodExpiryMonth(Integer value) {
    Objects.requireNonNull(value, "El mes de vencimiento no puede ser nulo");
    if (value < 1 || value > 12) {
      throw new IllegalArgumentException("El mes de vencimiento debe estar entre 1 y 12");
    }
    this.value = value;
  }
}
