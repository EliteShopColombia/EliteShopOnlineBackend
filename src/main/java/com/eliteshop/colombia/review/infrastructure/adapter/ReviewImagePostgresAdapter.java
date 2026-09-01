package com.eliteshop.colombia.review.infrastructure.adapter;

import com.eliteshop.colombia.review.domain.model.ReviewId;
import com.eliteshop.colombia.review.domain.model.ReviewImage;
import com.eliteshop.colombia.review.domain.model.ReviewImageId;
import com.eliteshop.colombia.review.domain.model.ReviewImageOrder;
import com.eliteshop.colombia.review.domain.model.ReviewImageUrl;
import com.eliteshop.colombia.review.domain.repository.ReviewImageRepository;
import com.eliteshop.colombia.review.infrastructure.persistence.ReviewImageEntity;
import com.eliteshop.colombia.review.infrastructure.persistence.ReviewImageJpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewImagePostgresAdapter implements ReviewImageRepository {

  private final ReviewImageJpaRepository jpaRepository;

  @Override
  public List<ReviewImage> findByReviewId(ReviewId reviewId) {
    return jpaRepository.findByReviewIdOrderByOrderAsc(reviewId.getValue()).stream()
        .map(this::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public ReviewImage save(ReviewImage image) {
    ReviewImageEntity entity = toEntity(image);
    ReviewImageEntity savedEntity = jpaRepository.save(entity);
    return toDomain(savedEntity);
  }

  @Override
  public void deleteByReviewId(ReviewId reviewId) {
    jpaRepository.deleteByReviewId(reviewId.getValue());
  }

  private ReviewImage toDomain(ReviewImageEntity entity) {
    if (entity == null) return null;
    return new ReviewImage(
        new ReviewImageId(entity.getId()),
        new ReviewId(entity.getReviewId()),
        new ReviewImageUrl(entity.getImageUrl()),
        new ReviewImageOrder(entity.getOrder()));
  }

  private ReviewImageEntity toEntity(ReviewImage image) {
    if (image == null) return null;
    ReviewImageEntity entity = new ReviewImageEntity();
    entity.setId(image.getId().getValue());
    entity.setReviewId(image.getReviewId().getValue());
    entity.setImageUrl(image.getImageUrl().getValue());
    entity.setOrder(image.getOrder().getValue());
    entity.setCreatedAt(LocalDateTime.now());
    return entity;
  }
}
