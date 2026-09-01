package com.eliteshop.colombia.customer.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class AvatarNotFoundException extends RuntimeException {
  public static final String CODE = "AVATAR_NOT_FOUND";

  public AvatarNotFoundException(String message) {
    super(message);
  }
}
