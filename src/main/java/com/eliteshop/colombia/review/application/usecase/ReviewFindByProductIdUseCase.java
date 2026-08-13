package com.eliteshop.colombia.review.application.usecase;

import com.eliteshop.colombia.review.domain.model.Review;
import com.eliteshop.colombia.review.domain.model.ReviewProductId;
import com.eliteshop.colombia.review.domain.repository.ReviewRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReviewFindByProductIdUseCase {

  private final ReviewRepository repository;

  public List<Review> execute(ReviewProductId productId) {
    return repository.findByProductId(productId);
  }
}
