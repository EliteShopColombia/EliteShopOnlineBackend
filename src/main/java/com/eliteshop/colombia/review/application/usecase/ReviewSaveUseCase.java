package com.eliteshop.colombia.review.application.usecase;

import com.eliteshop.colombia.order.domain.repository.OrderItemRepository;
import com.eliteshop.colombia.review.domain.model.Review;
import com.eliteshop.colombia.review.domain.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ReviewSaveUseCase {

  private final ReviewRepository repository;
  private final OrderItemRepository orderItemRepository;

  public void execute(Review review) {
    log.info(
        "Guardando reseña para producto: {}, cliente: {}",
        review.getProductId(),
        review.getCustomerId());

    boolean hasPurchased =
        orderItemRepository.existsVerifiedPurchase(
            review.getCustomerId().getValue(), review.getProductId().getValue());

    if (!hasPurchased) {
      log.error(
          "El cliente {} no ha comprado el producto {}",
          review.getCustomerId(),
          review.getProductId());
      throw new com.eliteshop.colombia.review.domain.exception.ReviewNotPurchasedException(
          "Solo puedes reseñar productos que hayas comprado");
    }

    repository.save(review);
    log.info("Reseña guardada exitosamente para producto: {}", review.getProductId());
  }
}
