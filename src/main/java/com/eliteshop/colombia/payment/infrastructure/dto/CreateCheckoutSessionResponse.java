package com.eliteshop.colombia.payment.infrastructure.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateCheckoutSessionResponse {
  private String sessionId;
  private String token;
  private String invoice;
}
