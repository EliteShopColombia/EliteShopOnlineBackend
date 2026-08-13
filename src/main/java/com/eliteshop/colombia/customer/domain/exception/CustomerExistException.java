package com.eliteshop.colombia.customer.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class CustomerExistException extends RuntimeException {
  public CustomerExistException(String message) {
    super(message);
  }
}
