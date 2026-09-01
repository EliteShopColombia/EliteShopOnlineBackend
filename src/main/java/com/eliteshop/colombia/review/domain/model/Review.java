package com.eliteshop.colombia.review.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
  private final List<ReviewImage> images;

  public Review(
      ReviewId id,
      ReviewProductId productId,
      ReviewCustomerId customerId,
      ReviewQualify qualify,
      ReviewContent content) {
    this(id, productId, customerId, qualify, content, new ArrayList<>());
  }

  public List<ReviewImage> getImages() {
    return images != null ? Collections.unmodifiableList(images) : Collections.emptyList();
  }
}
