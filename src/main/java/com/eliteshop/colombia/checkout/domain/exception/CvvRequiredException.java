package com.eliteshop.colombia.checkout.domain.exception;

public class CvvRequiredException extends RuntimeException {
  public CvvRequiredException(String message) {
    super(message);
  }
}
