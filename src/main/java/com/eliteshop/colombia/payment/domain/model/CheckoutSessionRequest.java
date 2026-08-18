package com.eliteshop.colombia.payment.domain.model;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckoutSessionRequest {
  private UUID orderId;
  private String storeName;
  private BigDecimal amount;
  private String currency;
  private String invoice;
  private String description;
  private String customerEmail;
  private String paymentMethod;
  private Map<String, Object> billing;
}
