package com.eliteshop.colombia.review.application.usecase;

import com.eliteshop.colombia.review.domain.model.Review;
import com.eliteshop.colombia.review.domain.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReviewSaveUseCase {

  private final ReviewRepository repository;

  public void execute(Review review) {
    repository.save(review);
  }
}
