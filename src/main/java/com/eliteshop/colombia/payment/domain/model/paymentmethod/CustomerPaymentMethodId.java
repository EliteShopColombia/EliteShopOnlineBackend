package com.eliteshop.colombia.payment.domain.model.paymentmethod;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class CustomerPaymentMethodId {
  private final UUID value;

  public CustomerPaymentMethodId(UUID value) {
    Objects.requireNonNull(value, "El ID del método de pago no puede ser nulo");
    this.value = value;
  }

  public static CustomerPaymentMethodId generate() {
    return new CustomerPaymentMethodId(UUID.randomUUID());
  }
}
