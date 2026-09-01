package com.eliteshop.colombia.review.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "review_image")
public class ReviewImageEntity {

  @Id
  @Column(name = "image_id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "review_id", nullable = false)
  private UUID reviewId;

  @Column(name = "image_url", nullable = false, columnDefinition = "TEXT")
  private String imageUrl;

  @Column(name = "image_order", nullable = false)
  private Integer order;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;
}
