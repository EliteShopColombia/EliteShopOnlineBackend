package com.eliteshop.colombia.checkout.domain.exception;

public class CannotBuyOwnStoreException extends RuntimeException {
  public CannotBuyOwnStoreException(String message) {
    super(message);
  }
}
