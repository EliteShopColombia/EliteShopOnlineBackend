package com.eliteshop.colombia.review.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.eliteshop.colombia.order.domain.repository.OrderItemRepository;
import com.eliteshop.colombia.review.application.usecase.*;
import com.eliteshop.colombia.review.domain.exception.ReviewNotFoundException;
import com.eliteshop.colombia.review.domain.exception.ReviewNotPurchasedException;
import com.eliteshop.colombia.review.domain.model.*;
import com.eliteshop.colombia.review.domain.repository.ReviewRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewUseCasesTest {

  @Mock private ReviewRepository reviewRepository;
  @Mock private OrderItemRepository orderItemRepository;

  private ReviewFindByIdUseCase findByIdUseCase;
  private ReviewDeleteUseCase deleteUseCase;
  private ReviewFindByProductIdUseCase findByProductIdUseCase;
  private ReviewSaveUseCase saveUseCase;
  private ReviewFindAllUseCase findAllUseCase;

  private UUID reviewId;
  private UUID productId;
  private UUID customerId;

  @BeforeEach
  void setUp() {
    findByIdUseCase = new ReviewFindByIdUseCase(reviewRepository);
    deleteUseCase = new ReviewDeleteUseCase(reviewRepository);
    findByProductIdUseCase = new ReviewFindByProductIdUseCase(reviewRepository);
    saveUseCase = new ReviewSaveUseCase(reviewRepository, orderItemRepository);
    findAllUseCase = new ReviewFindAllUseCase(reviewRepository);
    reviewId = UUID.randomUUID();
    productId = UUID.randomUUID();
    customerId = UUID.randomUUID();
  }

  // ==================== ReviewFindByIdUseCase ====================

  @Test
  void findById_shouldReturnEmptyWhenIdIsNull() {
    Optional<Review> result = findByIdUseCase.execute(null);
    assertThat(result).isEmpty();
    verifyNoInteractions(reviewRepository);
  }

  @Test
  void findById_shouldReturnReviewWhenExists() {
    Review review = buildReview();
    when(reviewRepository.findById(any(ReviewId.class))).thenReturn(Optional.of(review));

    Optional<Review> result = findByIdUseCase.execute(new ReviewId(reviewId));

    assertThat(result).isPresent();
  }

  @Test
  void findById_shouldReturnEmptyWhenNotFound() {
    when(reviewRepository.findById(any(ReviewId.class))).thenReturn(Optional.empty());

    Optional<Review> result = findByIdUseCase.execute(new ReviewId(reviewId));

    assertThat(result).isEmpty();
  }

  // ==================== ReviewDeleteUseCase ====================

  @Test
  void delete_shouldThrowWhenReviewNotFound() {
    when(reviewRepository.findById(any(ReviewId.class))).thenReturn(Optional.empty());

    assertThatThrownBy(() -> deleteUseCase.execute(new ReviewId(reviewId)))
        .isInstanceOf(ReviewNotFoundException.class);
  }

  @Test
  void delete_shouldDeleteWhenReviewExists() {
    Review review = buildReview();
    when(reviewRepository.findById(any(ReviewId.class))).thenReturn(Optional.of(review));

    deleteUseCase.execute(new ReviewId(reviewId));

    verify(reviewRepository).delete(any(ReviewId.class));
  }

  // ==================== ReviewFindByProductIdUseCase ====================

  @Test
  void findByProductId_shouldReturnReviewsForProduct() {
    Review review = buildReview();
    when(reviewRepository.findByProductId(any(ReviewProductId.class))).thenReturn(List.of(review));

    List<Review> result = findByProductIdUseCase.execute(new ReviewProductId(productId));

    assertThat(result).hasSize(1);
  }

  @Test
  void findByProductId_shouldReturnEmptyListWhenNoReviews() {
    when(reviewRepository.findByProductId(any(ReviewProductId.class))).thenReturn(List.of());

    List<Review> result = findByProductIdUseCase.execute(new ReviewProductId(productId));

    assertThat(result).isEmpty();
  }

  // ==================== ReviewSaveUseCase ====================

  @Test
  void save_shouldThrowWhenCustomerHasNotPurchased() {
    Review review = buildReview();
    when(orderItemRepository.existsVerifiedPurchase(any(UUID.class), any(UUID.class)))
        .thenReturn(false);

    assertThatThrownBy(() -> saveUseCase.execute(review))
        .isInstanceOf(ReviewNotPurchasedException.class);
  }

  @Test
  void save_shouldSaveWhenCustomerHasPurchased() {
    Review review = buildReview();
    when(orderItemRepository.existsVerifiedPurchase(any(UUID.class), any(UUID.class)))
        .thenReturn(true);

    saveUseCase.execute(review);

    verify(reviewRepository).save(any(Review.class));
  }

  // ==================== ReviewFindAllUseCase ====================

  @Test
  void findAll_shouldReturnAllReviews() {
    Review review = buildReview();
    when(reviewRepository.findAll()).thenReturn(List.of(review));

    List<Review> result = findAllUseCase.execute();

    assertThat(result).hasSize(1);
  }

  // ==================== Helpers ====================

  private Review buildReview() {
    return new Review(
        new ReviewId(reviewId),
        new ReviewProductId(productId),
        new ReviewCustomerId(customerId),
        new ReviewQualify(5),
        new ReviewContent("Excelente producto, muy buena calidad."));
  }
}
