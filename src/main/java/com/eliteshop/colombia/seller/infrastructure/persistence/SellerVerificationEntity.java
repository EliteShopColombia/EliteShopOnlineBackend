package com.eliteshop.colombia.seller.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "seller_verification")
public class SellerVerificationEntity {

  @Id
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "seller_id", nullable = false)
  private UUID sellerId;

  @Column(name = "verification_type", nullable = false, length = 50)
  private String verificationType;

  @Column(name = "document_type", nullable = false, length = 20)
  private String documentType;

  @Column(name = "document_number", length = 20)
  private String documentNumber;

  @Column(name = "document_minio_key")
  private String documentMinioKey;

  @Column(name = "selfie_minio_key")
  private String selfieMinioKey;

  @Column(name = "status", nullable = false, length = 30)
  private String status;

  @Column(name = "confidence_score")
  private Double confidenceScore;

  @Column(name = "rejection_reason")
  private String rejectionReason;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;
}
