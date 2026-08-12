package com.eliteshop.colombia.review.domain.repository;

import com.eliteshop.colombia.review.domain.model.Review;
import com.eliteshop.colombia.review.domain.model.ReviewId;
import com.eliteshop.colombia.review.domain.model.ReviewProductId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewRepository {
  Review save(Review review);

  void delete(ReviewId id);

  List<Review> findAll();

  Page<Review> findAll(Pageable pageable);

  Optional<Review> findById(ReviewId id);

  List<Review> findByProductId(ReviewProductId productId);
}
