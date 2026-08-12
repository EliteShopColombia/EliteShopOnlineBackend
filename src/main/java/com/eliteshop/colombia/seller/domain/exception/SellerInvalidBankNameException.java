package com.eliteshop.colombia.seller.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class SellerInvalidBankNameException extends RuntimeException {
  public SellerInvalidBankNameException(String message) {
    super(message);
  }
}
