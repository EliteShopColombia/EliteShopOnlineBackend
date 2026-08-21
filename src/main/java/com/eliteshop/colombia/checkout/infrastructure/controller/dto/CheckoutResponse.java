package com.eliteshop.colombia.checkout.infrastructure.controller.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutResponse {
  private UUID orderId;
  private UUID paymentId;
  private String status;
  private String epaycoRefId;
  private String invoice;
  private BigDecimal totalAmount;
}
