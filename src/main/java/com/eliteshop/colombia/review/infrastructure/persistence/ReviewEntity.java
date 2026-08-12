package com.eliteshop.colombia.review.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "product_review")
public class ReviewEntity {

  @Id
  @Column(name = "review_id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "product_id", nullable = false)
  private UUID productId;

  @Column(name = "customer_id", nullable = false)
  private UUID customerId;

  @Column(name = "product_qualify", nullable = true)
  private Integer qualify;

  @Column(name = "product_review_content", nullable = true, length = 255)
  private String content;

  @Column(name = "product_review_image", nullable = true, columnDefinition = "TEXT")
  private String image;
}
