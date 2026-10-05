package com.eliteshop.colombia.review.domain.model;

import com.eliteshop.colombia.review.domain.exception.ReviewInvalidQualifyException;
import lombok.Getter;

@Getter
public class ReviewQualify {

  private static final int MIN = 1;
  private static final int MAX = 5;

  private final Integer value;

  public ReviewQualify(Integer value) {
    this.value = value;
    if (value == null || value < MIN || value > MAX) {
      throw new ReviewInvalidQualifyException(
          "La calificación debe estar entre " + MIN + " y " + MAX);
    }
  }
}
