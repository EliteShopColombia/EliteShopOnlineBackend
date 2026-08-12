package com.eliteshop.colombia.seller.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class SellerAlreadyExistsException extends RuntimeException {
  public SellerAlreadyExistsException(String message) {
    super(message);
  }
}
