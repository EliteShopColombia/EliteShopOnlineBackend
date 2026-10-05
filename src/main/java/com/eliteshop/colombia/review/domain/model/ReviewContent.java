package com.eliteshop.colombia.review.domain.model;

import com.eliteshop.colombia.review.domain.exception.ReviewInvalidContentException;
import lombok.Getter;

@Getter
public class ReviewContent {

  /** La columna en base de datos es VARCHAR(255); se valida aquí para no llegar a un 500. */
  private static final int MAX_LENGTH = 255;

  private final String value;

  public ReviewContent(String value) {
    if (value != null && value.length() > MAX_LENGTH) {
      throw new ReviewInvalidContentException(
          "El contenido de la reseña no puede superar " + MAX_LENGTH + " caracteres");
    }
    this.value = value;
  }
}
