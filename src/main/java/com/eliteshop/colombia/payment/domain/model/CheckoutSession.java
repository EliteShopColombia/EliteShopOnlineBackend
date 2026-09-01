package com.eliteshop.colombia.payment.domain.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckoutSession {
  private String sessionId;
  private String token;
  private String invoice;
}
