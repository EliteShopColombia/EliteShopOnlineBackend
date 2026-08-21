package com.eliteshop.colombia.checkout.domain.exception;

public class CannotBuyOwnProductException extends RuntimeException {
  public CannotBuyOwnProductException(String message) {
    super(message);
  }
}
