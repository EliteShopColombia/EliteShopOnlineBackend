package com.eliteshop.colombia.review.infrastructure.mapper;

import com.eliteshop.colombia.review.domain.model.*;
import com.eliteshop.colombia.review.infrastructure.controller.dto.ReviewRequest;
import com.eliteshop.colombia.review.infrastructure.controller.dto.ReviewResponse;
import com.eliteshop.colombia.review.infrastructure.persistence.ReviewEntity;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

  public Review toDomain(ReviewEntity entity) {
    if (entity == null) return null;

    return new Review(
        new ReviewId(entity.getId()),
        new ReviewProductId(entity.getProductId()),
        new ReviewCustomerId(entity.getCustomerId()),
        entity.getQualify() != null ? new ReviewQualify(entity.getQualify()) : null,
        entity.getContent() != null ? new ReviewContent(entity.getContent()) : null,
        entity.getImage() != null ? new ReviewImage(entity.getImage()) : null);
  }

  public ReviewEntity toEntity(Review domain) {
    if (domain == null) return null;

    ReviewEntity entity = new ReviewEntity();
    entity.setId(domain.getId().getValue());
    entity.setProductId(domain.getProductId().getValue());
    entity.setCustomerId(domain.getCustomerId().getValue());
    if (domain.getQualify() != null) {
      entity.setQualify(domain.getQualify().getValue());
    }
    if (domain.getContent() != null) {
      entity.setContent(domain.getContent().getValue());
    }
    if (domain.getImage() != null) {
      entity.setImage(domain.getImage().getValue());
    }

    return entity;
  }

  public Review toDomainFromRequest(ReviewRequest request) {
    if (request == null) return null;

    return new Review(
        ReviewId.generate(),
        new ReviewProductId(request.getProductId()),
        new ReviewCustomerId(request.getCustomerId()),
        request.getQualify() != null ? new ReviewQualify(request.getQualify()) : null,
        request.getContent() != null ? new ReviewContent(request.getContent()) : null,
        request.getImage() != null ? new ReviewImage(request.getImage()) : null);
  }

  public ReviewResponse toResponse(Review domain) {
    if (domain == null) return null;

    ReviewResponse response = new ReviewResponse();
    response.setId(domain.getId().getValue());
    response.setProductId(domain.getProductId().getValue());
    response.setCustomerId(domain.getCustomerId().getValue());
    if (domain.getQualify() != null) {
      response.setQualify(domain.getQualify().getValue());
    }
    if (domain.getContent() != null) {
      response.setContent(domain.getContent().getValue());
    }
    if (domain.getImage() != null) {
      response.setImage(domain.getImage().getValue());
    }

    return response;
  }
}
