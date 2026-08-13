package com.eliteshop.colombia.review.domain.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class Review {
  @NonNull private final ReviewId id;
  @NonNull private final ReviewProductId productId;
  @NonNull private final ReviewCustomerId customerId;
  private final ReviewQualify qualify;
  private final ReviewContent content;
  private final ReviewImage image;
}
