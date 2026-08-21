package com.eliteshop.colombia.seller.application.usecase;

import com.eliteshop.colombia.seller.domain.event.SellerVerificationCompletedEvent;
import com.eliteshop.colombia.seller.domain.model.Seller;
import com.eliteshop.colombia.seller.domain.model.SellerId;
import com.eliteshop.colombia.seller.domain.model.verification.*;
import com.eliteshop.colombia.seller.domain.repository.SellerRepository;
import com.eliteshop.colombia.seller.infrastructure.adapter.FaceMatcherAdapter;
import com.eliteshop.colombia.seller.infrastructure.adapter.MinIOAdapter;
import java.io.InputStream;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

@Slf4j
@RequiredArgsConstructor
public class VerifySellerUseCase {

  private final SellerVerificationRepository repository;
  private final SellerRepository sellerRepository;
  private final MinIOAdapter minIOAdapter;
  private final FaceMatcherAdapter faceMatcherAdapter;
  private final ApplicationEventPublisher eventPublisher;

  public SellerVerification uploadDocument(UUID sellerId, String filename, InputStream stream) {
    log.info("Subiendo documento de verificacion para vendedor sellerId={}", sellerId);
    String objectKey = minIOAdapter.uploadDocument(sellerId.toString(), filename, stream);

    Seller seller =
        sellerRepository
            .findById(new SellerId(sellerId))
            .orElseThrow(
                () -> {
                  log.error("Vendedor no encontrado sellerId={}", sellerId);
                  return new RuntimeException("Vendedor no encontrado");
                });

    SellerVerification verification =
        repository
            .findBySellerId(sellerId)
            .orElseGet(() -> SellerVerification.create(new SellerVerificationSellerId(sellerId)));

    verification =
        verification.withDocumentUploaded(
            new SellerVerificationDocumentMinioKey(objectKey),
            new SellerVerificationDocumentNumber(seller.getDniNumber().getValue()));
    log.info("Documento subido exitosamente para vendedor sellerId={}", sellerId);
    return repository.save(verification);
  }

  public SellerVerification uploadSelfie(UUID sellerId, String filename, InputStream stream) {
    log.info("Subiendo selfie de verificacion para vendedor sellerId={}", sellerId);
    String objectKey = minIOAdapter.uploadSelfie(sellerId.toString(), filename, stream);

    SellerVerification verification =
        repository
            .findBySellerId(sellerId)
            .orElseThrow(
                () -> {
                  log.error(
                      "No existe verificacion pendiente para sellerId={}, suba la cedula primero",
                      sellerId);
                  return new RuntimeException("Sube la cedula primero");
                });

    verification = verification.withSelfieUploaded(new SellerVerificationSelfieMinioKey(objectKey));
    log.info("Selfie subida exitosamente para vendedor sellerId={}", sellerId);
    return repository.save(verification);
  }

  public SellerVerification validate(UUID sellerId) {
    log.info("Validando verificacion para vendedor sellerId={}", sellerId);
    SellerVerification verification =
        repository
            .findBySellerId(sellerId)
            .orElseThrow(
                () -> {
                  log.error("No hay verificacion pendiente para sellerId={}", sellerId);
                  return new RuntimeException("No hay verificacion pendiente");
                });

    if (!"SELFIE_UPLOADED".equals(verification.getStatus().getValue())) {
      log.error(
          "Verificacion en estado incorrecto para sellerId={}: {}",
          sellerId,
          verification.getStatus().getValue());
      throw new RuntimeException("Primero sube la cedula y la selfie");
    }

    String selfieObject = verification.getSelfieMinioKey().getValue();
    String documentObject = verification.getDocumentMinioKey().getValue();

    FaceMatcherAdapter.FaceMatchResult result =
        faceMatcherAdapter.match(selfieObject, documentObject);

    boolean isApproved = result.match() && result.confidence() >= 0.6;

    if (isApproved) {
      verification =
          verification.approved(new SellerVerificationConfidenceScore(result.confidence()));
    } else {
      verification = verification.rejected(new SellerVerificationRejectionReason(result.message()));
    }

    SellerVerification savedVerification = repository.save(verification);

    eventPublisher.publishEvent(
        new SellerVerificationCompletedEvent(
            sellerId, isApproved, result.confidence(), result.message(), Instant.now()));

    log.info(
        "Verificacion completada para sellerId={}, aprobado={}, confianza={}",
        sellerId,
        isApproved,
        result.confidence());
    return savedVerification;
  }

  public SellerVerification getStatus(UUID sellerId) {
    log.info("Consultando estado de verificacion para vendedor sellerId={}", sellerId);
    return repository
        .findBySellerId(sellerId)
        .orElseThrow(
            () -> {
              log.error("No hay verificacion para sellerId={}", sellerId);
              return new RuntimeException("No hay verificacion para este vendedor");
            });
  }
}
