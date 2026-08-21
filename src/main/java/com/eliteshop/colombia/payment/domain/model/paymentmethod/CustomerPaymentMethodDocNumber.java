package com.eliteshop.colombia.payment.domain.model.paymentmethod;

import lombok.Getter;

@Getter
public class CustomerPaymentMethodDocNumber {
  private final String value;

  public CustomerPaymentMethodDocNumber(String value) {
    this.value = (value == null || value.isBlank()) ? null : value.trim();
  }
}
