package com.eliteshop.colombia.payment.infrastructure.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import lombok.Data;

@Data
public class CreateCheckoutSessionRequest {

  @NotNull(message = "El orderId es requerido")
  private UUID orderId;

  @NotNull(message = "El amount es requerido")
  private BigDecimal amount;

  private String customerEmail;
  private String paymentMethod;
  private Map<String, Object> billing;
}
