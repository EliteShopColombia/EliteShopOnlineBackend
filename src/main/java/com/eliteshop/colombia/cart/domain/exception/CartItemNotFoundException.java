package com.eliteshop.colombia.cart.domain.exception;

public class CartItemNotFoundException extends RuntimeException {
  public CartItemNotFoundException(String message) {
    super(message);
  }
}
