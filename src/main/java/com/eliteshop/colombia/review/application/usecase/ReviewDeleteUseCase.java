package com.eliteshop.colombia.review.application.usecase;

import com.eliteshop.colombia.review.domain.exception.ReviewNotFoundException;
import com.eliteshop.colombia.review.domain.model.ReviewId;
import com.eliteshop.colombia.review.domain.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ReviewDeleteUseCase {

  private final ReviewRepository repository;

  public void execute(ReviewId id) {
    log.info("Iniciando eliminación de reseña con id: {}", id);

    if (repository.findById(id).isEmpty()) {
      log.error("Reseña no encontrada con id: {}", id);
      throw new ReviewNotFoundException("La reseña no existe en la plataforma");
    }

    repository.delete(id);
    log.info("Reseña eliminada exitosamente con id: {}", id);
  }
}
