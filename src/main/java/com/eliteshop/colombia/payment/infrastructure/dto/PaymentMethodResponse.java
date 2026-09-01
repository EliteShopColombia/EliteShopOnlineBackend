package com.eliteshop.colombia.payment.infrastructure.dto;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentMethodResponse {
  private UUID id;
  private String last4;
  private String brand;
  private Integer expiryMonth;
  private Integer expiryYear;
  private boolean isDefault;
}
