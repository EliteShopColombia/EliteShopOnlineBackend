package com.eliteshop.colombia.review.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** La calificación de una reseña está fuera del rango permitido (1-5). */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ReviewInvalidQualifyException extends RuntimeException {

  public static final String CODE = "REVIEW_INVALID_QUALIFY";

  public ReviewInvalidQualifyException(String message) {
    super(message);
  }
}
