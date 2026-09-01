package com.eliteshop.colombia.payment.domain.model.paymentmethod;

import lombok.Getter;

@Getter
public class CustomerPaymentMethodDocType {
  private final String value;

  public CustomerPaymentMethodDocType(String value) {
    this.value = (value == null || value.isBlank()) ? null : value.trim();
  }
}
