package com.eliteshop.colombia.checkout.domain.exception;

public class PaymentFailedException extends RuntimeException {
  public PaymentFailedException(String message) {
    super(message);
  }
}
