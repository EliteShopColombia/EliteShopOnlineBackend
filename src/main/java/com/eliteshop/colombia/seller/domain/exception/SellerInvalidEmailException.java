package com.eliteshop.colombia.seller.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class SellerInvalidEmailException extends RuntimeException {
  public SellerInvalidEmailException(String message) {
    super(message);
  }
}
