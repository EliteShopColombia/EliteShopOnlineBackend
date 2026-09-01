package com.eliteshop.colombia.review.infrastructure.mapper;

import com.eliteshop.colombia.review.domain.model.*;
import com.eliteshop.colombia.review.infrastructure.controller.dto.ReviewRequest;
import com.eliteshop.colombia.review.infrastructure.controller.dto.ReviewResponse;
import com.eliteshop.colombia.review.infrastructure.persistence.ReviewEntity;
import com.eliteshop.colombia.review.infrastructure.persistence.ReviewImageEntity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;
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
        new ArrayList<>());
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

    return entity;
  }

  public Review toDomainFromRequest(ReviewRequest request) {
    if (request == null) return null;
    return toDomainFromRequest(request, request.getCustomerId());
  }

  public Review toDomainFromRequest(ReviewRequest request, java.util.UUID customerId) {
    if (request == null) return null;

    return new Review(
        ReviewId.generate(),
        new ReviewProductId(request.getProductId()),
        new ReviewCustomerId(customerId),
        request.getQualify() != null ? new ReviewQualify(request.getQualify()) : null,
        request.getContent() != null ? new ReviewContent(request.getContent()) : null,
        new ArrayList<>());
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
    if (domain.getImages() != null) {
      response.setImages(
          domain.getImages().stream()
              .map(img -> img.getImageUrl().getValue())
              .collect(Collectors.toList()));
    } else {
      response.setImages(Collections.emptyList());
    }

    return response;
  }

  public ReviewImage toDomain(ReviewImageEntity entity) {
    if (entity == null) return null;
    return new ReviewImage(
        new ReviewImageId(entity.getId()),
        new ReviewId(entity.getReviewId()),
        new ReviewImageUrl(entity.getImageUrl()),
        new ReviewImageOrder(entity.getOrder()));
  }

  public ReviewImageEntity toEntity(ReviewImage domain) {
    if (domain == null) return null;
    ReviewImageEntity entity = new ReviewImageEntity();
    entity.setId(domain.getId().getValue());
    entity.setReviewId(domain.getReviewId().getValue());
    entity.setImageUrl(domain.getImageUrl().getValue());
    entity.setOrder(domain.getOrder().getValue());
    return entity;
  }
}
