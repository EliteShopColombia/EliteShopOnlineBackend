package com.eliteshop.colombia.payment.infrastructure.dto;

import java.util.UUID;
import lombok.Data;

@Data
public class RetryPaymentRequest {
  private UUID paymentMethodId;
  private String cvv;
}
