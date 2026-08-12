package com.eliteshop.colombia.review.infrastructure.controller.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewRequest {

  @NotNull(message = "El ID del producto es obligatorio")
  private UUID productId;

  @NotNull(message = "El ID del cliente es obligatorio")
  private UUID customerId;

  private Integer qualify;

  private String content;

  private String image;
}
