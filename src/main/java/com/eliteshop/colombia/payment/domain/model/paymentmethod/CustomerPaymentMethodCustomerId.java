package com.eliteshop.colombia.payment.domain.model.paymentmethod;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class CustomerPaymentMethodCustomerId {
  private final UUID value;

  public CustomerPaymentMethodCustomerId(UUID value) {
    Objects.requireNonNull(value, "El ID del customer no puede ser nulo");
    this.value = value;
  }
}
