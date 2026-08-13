package com.eliteshop.colombia.review.application.usecase;

import com.eliteshop.colombia.review.domain.model.Review;
import com.eliteshop.colombia.review.domain.model.ReviewId;
import com.eliteshop.colombia.review.domain.repository.ReviewRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReviewFindByIdUseCase {

  private final ReviewRepository repository;

  public Optional<Review> execute(ReviewId id) {
    if (id == null) return Optional.empty();

    return repository.findById(id);
  }
}
