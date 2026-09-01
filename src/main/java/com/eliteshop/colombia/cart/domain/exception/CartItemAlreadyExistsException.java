package com.eliteshop.colombia.cart.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class CartItemAlreadyExistsException extends RuntimeException {
  public static final String CODE = "CART_ITEM_ALREADY_EXISTS";

  public CartItemAlreadyExistsException(String message) {
    super(message);
  }
}
