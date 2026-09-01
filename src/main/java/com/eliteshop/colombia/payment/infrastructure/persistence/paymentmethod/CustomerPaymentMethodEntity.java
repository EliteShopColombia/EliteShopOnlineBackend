package com.eliteshop.colombia.payment.infrastructure.persistence.paymentmethod;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "customer_payment_method")
public class CustomerPaymentMethodEntity {

  @Id
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "customer_id", nullable = false)
  private UUID customerId;

  @Column(name = "epayco_token", nullable = false)
  private String epaycoToken;

  @Column(name = "epayco_customer_id", nullable = false)
  private String epaycoCustomerId;

  @Column(name = "last4", nullable = false, length = 4)
  private String last4;

  @Column(name = "brand", nullable = false, length = 20)
  private String brand;

  @Column(name = "expiry_month", nullable = false)
  private Integer expiryMonth;

  @Column(name = "expiry_year", nullable = false)
  private Integer expiryYear;

  @Column(name = "doc_type", length = 10)
  private String docType;

  @Column(name = "doc_number", length = 30)
  private String docNumber;

  @Column(name = "is_default", nullable = false)
  private boolean isDefault;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;
}
