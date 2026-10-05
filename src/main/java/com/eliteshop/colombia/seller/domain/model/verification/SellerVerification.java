package com.eliteshop.colombia.seller.domain.model.verification;

import java.time.Instant;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class SellerVerification {
  @NonNull private final SellerVerificationId id;
  @NonNull private final SellerVerificationSellerId sellerId;
  @NonNull private final SellerVerificationType verificationType;
  @NonNull private final SellerVerificationDocumentType documentType;
  private final SellerVerificationDocumentNumber documentNumber;
  private final SellerVerificationDocumentMinioKey documentMinioKey;
  private final SellerVerificationSelfieMinioKey selfieMinioKey;
  @NonNull private final SellerVerificationStatus status;
  private final SellerVerificationConfidenceScore confidenceScore;
  private final SellerVerificationRejectionReason rejectionReason;
  @NonNull private final SellerVerificationCreatedAt createdAt;
  private final SellerVerificationUpdatedAt updatedAt;
  // Async verification fields
  private final SellerVerificationTaskId taskId;
  private final SellerVerificationLivenessConfidence livenessConfidence;
  private final SellerVerificationAntispoofScore antispoofScore;
  private final SellerVerificationOcrCedulaNumber ocrCedulaNumber;
  private final SellerVerificationOcrCedulaName ocrCedulaName;

  public static SellerVerification create(SellerVerificationSellerId sellerId) {
    return new SellerVerification(
        SellerVerificationId.generate(),
        sellerId,
        new SellerVerificationType("FACIAL_MATCH"),
        new SellerVerificationDocumentType("CC"),
        null,
        null,
        null,
        SellerVerificationStatus.pending(),
        null,
        null,
        new SellerVerificationCreatedAt(Instant.now()),
        null,
        null,
        null,
        null,
        null,
        null);
  }

  public SellerVerification withDocumentUploaded(
      SellerVerificationDocumentMinioKey documentMinioKey,
      SellerVerificationDocumentNumber documentNumber) {
    return new SellerVerification(
        this.id,
        this.sellerId,
        this.verificationType,
        this.documentType,
        documentNumber,
        documentMinioKey,
        this.selfieMinioKey,
        SellerVerificationStatus.documentUploaded(),
        this.confidenceScore,
        this.rejectionReason,
        this.createdAt,
        new SellerVerificationUpdatedAt(Instant.now()),
        this.taskId,
        this.livenessConfidence,
        this.antispoofScore,
        this.ocrCedulaNumber,
        this.ocrCedulaName);
  }

  public SellerVerification withSelfieUploaded(SellerVerificationSelfieMinioKey selfieMinioKey) {
    return new SellerVerification(
        this.id,
        this.sellerId,
        this.verificationType,
        this.documentType,
        this.documentNumber,
        this.documentMinioKey,
        selfieMinioKey,
        SellerVerificationStatus.selfieUploaded(),
        this.confidenceScore,
        this.rejectionReason,
        this.createdAt,
        new SellerVerificationUpdatedAt(Instant.now()),
        this.taskId,
        this.livenessConfidence,
        this.antispoofScore,
        this.ocrCedulaNumber,
        this.ocrCedulaName);
  }

  /**
   * Transición a estado PROCESSING: la tarea fue enviada al microservicio y se obtuvo un task_id.
   */
  public SellerVerification withProcessing(SellerVerificationTaskId taskId) {
    return new SellerVerification(
        this.id,
        this.sellerId,
        this.verificationType,
        this.documentType,
        this.documentNumber,
        this.documentMinioKey,
        this.selfieMinioKey,
        SellerVerificationStatus.processing(),
        this.confidenceScore,
        this.rejectionReason,
        this.createdAt,
        new SellerVerificationUpdatedAt(Instant.now()),
        taskId,
        this.livenessConfidence,
        this.antispoofScore,
        this.ocrCedulaNumber,
        this.ocrCedulaName);
  }

  public SellerVerification approved(SellerVerificationConfidenceScore confidenceScore) {
    return new SellerVerification(
        this.id,
        this.sellerId,
        this.verificationType,
        this.documentType,
        this.documentNumber,
        this.documentMinioKey,
        this.selfieMinioKey,
        SellerVerificationStatus.approved(),
        confidenceScore,
        null,
        this.createdAt,
        new SellerVerificationUpdatedAt(Instant.now()),
        this.taskId,
        this.livenessConfidence,
        this.antispoofScore,
        this.ocrCedulaNumber,
        this.ocrCedulaName);
  }

  public SellerVerification rejected(
      SellerVerificationConfidenceScore confidenceScore,
      SellerVerificationRejectionReason rejectionReason) {
    return new SellerVerification(
        this.id,
        this.sellerId,
        this.verificationType,
        this.documentType,
        this.documentNumber,
        this.documentMinioKey,
        this.selfieMinioKey,
        SellerVerificationStatus.rejected(),
        confidenceScore,
        rejectionReason,
        this.createdAt,
        new SellerVerificationUpdatedAt(Instant.now()),
        this.taskId,
        this.livenessConfidence,
        this.antispoofScore,
        this.ocrCedulaNumber,
        this.ocrCedulaName);
  }

  /**
   * Resultado final del microservicio con todas las métricas detalladas (OCR, liveness,
   * anti-spoofing).
   */
  public SellerVerification withDetailedResult(
      SellerVerificationConfidenceScore confidenceScore,
      SellerVerificationLivenessConfidence livenessConfidence,
      SellerVerificationAntispoofScore antispoofScore,
      SellerVerificationOcrCedulaNumber ocrCedulaNumber,
      SellerVerificationOcrCedulaName ocrCedulaName) {
    return new SellerVerification(
        this.id,
        this.sellerId,
        this.verificationType,
        this.documentType,
        this.documentNumber,
        this.documentMinioKey,
        this.selfieMinioKey,
        this.status,
        confidenceScore,
        this.rejectionReason,
        this.createdAt,
        new SellerVerificationUpdatedAt(Instant.now()),
        this.taskId,
        livenessConfidence,
        antispoofScore,
        ocrCedulaNumber,
        ocrCedulaName);
  }
}
