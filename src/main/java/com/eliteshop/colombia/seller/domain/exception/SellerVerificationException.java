package com.eliteshop.colombia.seller.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class SellerVerificationException extends RuntimeException {
  public static final String CODE = "SELLER_VERIFICATION_FAILED";

  public SellerVerificationException(String message) {
    super(message);
  }
}
