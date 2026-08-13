package com.eliteshop.colombia.seller.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class SellerInvalidDniNumberException extends RuntimeException {
  public SellerInvalidDniNumberException(String message) {
    super(message);
  }
}
