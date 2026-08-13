package com.eliteshop.colombia.review.application.usecase;

import com.eliteshop.colombia.review.domain.exception.ReviewNotFoundException;
import com.eliteshop.colombia.review.domain.model.ReviewId;
import com.eliteshop.colombia.review.domain.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReviewDeleteUseCase {

  private final ReviewRepository repository;

  public void execute(ReviewId id) {
    if (repository.findById(id).isEmpty()) {
      throw new ReviewNotFoundException("La reseña no existe en la plataforma");
    }

    repository.delete(id);
  }
}
