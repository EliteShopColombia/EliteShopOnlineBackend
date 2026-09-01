package com.eliteshop.colombia.review.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ReviewImage {
  private final ReviewImageId id;
  private final ReviewId reviewId;
  private final ReviewImageUrl imageUrl;
  private final ReviewImageOrder order;
}
