package com.eliteshop.colombia.review.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewImageJpaRepository extends JpaRepository<ReviewImageEntity, UUID> {
  List<ReviewImageEntity> findByReviewIdOrderByOrderAsc(UUID reviewId);

  void deleteByReviewId(UUID reviewId);
}
