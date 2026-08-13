package com.eliteshop.colombia.review.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewJpaRepository extends JpaRepository<ReviewEntity, UUID> {
  List<ReviewEntity> findByProductId(UUID productId);
}
