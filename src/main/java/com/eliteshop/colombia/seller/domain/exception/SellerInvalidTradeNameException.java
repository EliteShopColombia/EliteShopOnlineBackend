package com.eliteshop.colombia.seller.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class SellerInvalidTradeNameException extends RuntimeException {
  public SellerInvalidTradeNameException(String message) {
    super(message);
  }
}
