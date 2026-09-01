package com.eliteshop.colombia.payment.domain.exception;

public class PaymentAlreadyProcessedException extends RuntimeException {
  public PaymentAlreadyProcessedException(String message) {
    super(message);
  }
}
