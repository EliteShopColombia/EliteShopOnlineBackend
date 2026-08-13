package com.eliteshop.colombia.seller.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class SellerNotFoundException extends RuntimeException {
  public SellerNotFoundException(String message) {
    super(message);
  }
}
