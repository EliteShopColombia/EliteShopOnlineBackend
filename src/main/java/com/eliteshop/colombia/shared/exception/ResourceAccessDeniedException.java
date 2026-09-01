package com.eliteshop.colombia.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Indicates that the authenticated user cannot access the requested resource. */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ResourceAccessDeniedException extends RuntimeException {

  public ResourceAccessDeniedException(String message) {
    super(message);
  }
}
