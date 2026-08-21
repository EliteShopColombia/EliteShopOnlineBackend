package com.eliteshop.colombia.checkout.domain.exception;

public class EmptyCartException extends RuntimeException {
  public EmptyCartException(String message) {
    super(message);
  }
}
