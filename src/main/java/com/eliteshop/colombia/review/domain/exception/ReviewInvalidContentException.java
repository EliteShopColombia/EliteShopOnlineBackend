package com.eliteshop.colombia.review.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** El contenido de una reseña supera la longitud permitida. */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ReviewInvalidContentException extends RuntimeException {

  public static final String CODE = "REVIEW_INVALID_CONTENT";

  public ReviewInvalidContentException(String message) {
    super(message);
  }
}
