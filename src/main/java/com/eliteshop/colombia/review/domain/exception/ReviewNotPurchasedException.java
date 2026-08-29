package com.eliteshop.colombia.review.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class ReviewNotPurchasedException extends RuntimeException {
  public static final String CODE = "REVIEW_NOT_PURCHASED";

  public ReviewNotPurchasedException(String message) {
    super(message);
  }
}
