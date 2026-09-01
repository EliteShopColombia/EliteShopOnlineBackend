package com.eliteshop.colombia.review.application.usecase;

import com.eliteshop.colombia.review.domain.model.Review;
import com.eliteshop.colombia.review.domain.model.ReviewId;
import com.eliteshop.colombia.review.domain.repository.ReviewRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ReviewFindByIdUseCase {

  private final ReviewRepository repository;

  public Optional<Review> execute(ReviewId id) {
    log.info("Buscando reseña con id: {}", id);

    if (id == null) {
      log.error("Se recibió id nulo para buscar reseña");
      return Optional.empty();
    }

    Optional<Review> review = repository.findById(id);
    log.info("Reseña encontrada: {}", review.isPresent());
    return review;
  }
}
