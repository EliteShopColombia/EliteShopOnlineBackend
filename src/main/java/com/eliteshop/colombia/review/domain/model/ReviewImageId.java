package com.eliteshop.colombia.review.domain.model;

import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ReviewImageId {
  private final UUID value;

  public static ReviewImageId generate() {
    return new ReviewImageId(UUID.randomUUID());
  }
}
