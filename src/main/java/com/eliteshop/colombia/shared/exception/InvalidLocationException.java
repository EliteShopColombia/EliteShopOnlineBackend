package com.eliteshop.colombia.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Thrown when a department or city name does not exist in the location tables. */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidLocationException extends RuntimeException {

  public InvalidLocationException(String message) {
    super(message);
  }
}
