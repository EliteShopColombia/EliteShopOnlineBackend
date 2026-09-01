package com.eliteshop.colombia.customer.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class CustomerNotFoundException extends RuntimeException {
  public static final String CODE = "CUSTOMER_NOT_FOUND";

  public CustomerNotFoundException(String message) {
    super(message);
  }
}
