package com.eliteshop.colombia.review.infrastructure.controller.dto;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewResponse {

  private UUID id;
  private UUID productId;
  private UUID customerId;
  private Integer qualify;
  private String content;
  private String image;
}
