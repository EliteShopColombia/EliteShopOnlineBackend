package com.eliteshop.colombia.payment.infrastructure.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConfirmPaymentResponse {
  private String status;
  private String refId;
  private String invoice;
}
