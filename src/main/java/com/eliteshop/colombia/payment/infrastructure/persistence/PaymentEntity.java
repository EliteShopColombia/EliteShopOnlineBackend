package com.eliteshop.colombia.payment.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(name = "payment_info")
public class PaymentEntity {

  @Id
  @Column(name = "payment_id")
  private UUID id;

  @Column(name = "order_id", nullable = false)
  private UUID orderId;

  @Column(name = "seller_id")
  private UUID sellerId;

  @Column(nullable = false)
  private BigDecimal amount;

  @Column(length = 3)
  private String currency;

  @Column(name = "payment_method", length = 50)
  private String paymentMethod;

  @Column(name = "payment_status", nullable = false, length = 50)
  private String paymentStatus;

  @Column(name = "epayco_ref_id")
  private String epaycoRefId;

  @Column(name = "session_id")
  private String sessionId;

  @Column(unique = true)
  private String invoice;

  @Column(name = "customer_email")
  private String customerEmail;

  @Column(name = "platform_fee")
  private BigDecimal platformFee;

  @Column(name = "seller_amount")
  private BigDecimal sellerAmount;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
}
