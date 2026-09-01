package com.eliteshop.colombia.review.domain.repository;

import com.eliteshop.colombia.review.domain.model.ReviewId;
import com.eliteshop.colombia.review.domain.model.ReviewImage;
import java.util.List;

public interface ReviewImageRepository {
  List<ReviewImage> findByReviewId(ReviewId reviewId);

  ReviewImage save(ReviewImage image);

  void deleteByReviewId(ReviewId reviewId);
}
