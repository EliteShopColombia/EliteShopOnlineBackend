package com.eliteshop.colombia.payment.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Payment {

  private UUID id;
  private UUID orderId;
  private UUID sellerId;
  private BigDecimal amount;
  private String currency; // Should be Enum ideally but kept as String for simplicity based on plan
  private PaymentMethod method;
  private PaymentStatus status;
  private String epaycoRefId;
  private String sessionId;
  private String invoice;
  private String customerEmail;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  private BigDecimal platformFee;
  private BigDecimal sellerAmount;

  public static UUID generateId() {
    return UUID.randomUUID();
  }
}
