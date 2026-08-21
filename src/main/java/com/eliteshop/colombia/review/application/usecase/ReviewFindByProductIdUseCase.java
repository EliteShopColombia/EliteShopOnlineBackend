package com.eliteshop.colombia.review.application.usecase;

import com.eliteshop.colombia.review.domain.model.Review;
import com.eliteshop.colombia.review.domain.model.ReviewProductId;
import com.eliteshop.colombia.review.domain.repository.ReviewRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ReviewFindByProductIdUseCase {

  private final ReviewRepository repository;

  public List<Review> execute(ReviewProductId productId) {
    log.info("Buscando reseñas por producto con id: {}", productId);
    List<Review> reviews = repository.findByProductId(productId);
    log.info("Se encontraron {} reseñas para el producto", reviews.size());
    return reviews;
  }
}
