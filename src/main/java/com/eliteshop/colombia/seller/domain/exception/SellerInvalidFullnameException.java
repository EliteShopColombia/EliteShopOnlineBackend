package com.eliteshop.colombia.seller.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class SellerInvalidFullnameException extends RuntimeException {
  public SellerInvalidFullnameException(String message) {
    super(message);
  }
}
