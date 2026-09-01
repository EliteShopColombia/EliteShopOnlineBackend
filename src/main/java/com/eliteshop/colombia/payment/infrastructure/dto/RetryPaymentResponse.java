package com.eliteshop.colombia.payment.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetryPaymentResponse {
  private String status;
  private String invoice;
  private String refId;
}
