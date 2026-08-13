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
import org.springframework.context.ApplicationEventPublisher;

@RequiredArgsConstructor
public class VerifySellerUseCase {

  private final SellerVerificationRepository repository;
  private final SellerRepository sellerRepository;
  private final MinIOAdapter minIOAdapter;
  private final FaceMatcherAdapter faceMatcherAdapter;
  private final ApplicationEventPublisher eventPublisher;

  public SellerVerification uploadDocument(UUID sellerId, String filename, InputStream stream) {
    String objectKey = minIOAdapter.uploadDocument(sellerId.toString(), filename, stream);

    Seller seller =
            sellerRepository
                    .findById(new SellerId(sellerId))
                    .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));

    SellerVerification verification =
            repository
                    .findBySellerId(sellerId)
                    .orElseGet(() -> SellerVerification.create(new SellerVerificationSellerId(sellerId)));

    verification = verification.withDocumentUploaded(
            new SellerVerificationDocumentMinioKey(objectKey),
            new SellerVerificationDocumentNumber(seller.getDniNumber().getValue())
    );
    return repository.save(verification);
  }

  public SellerVerification uploadSelfie(UUID sellerId, String filename, InputStream stream) {
    String objectKey = minIOAdapter.uploadSelfie(sellerId.toString(), filename, stream);

    SellerVerification verification =
            repository
                    .findBySellerId(sellerId)
                    .orElseThrow(() -> new RuntimeException("Sube la cedula primero"));

    verification = verification.withSelfieUploaded(
            new SellerVerificationSelfieMinioKey(objectKey));
    return repository.save(verification);
  }

  public SellerVerification validate(UUID sellerId) {
    SellerVerification verification =
            repository
                    .findBySellerId(sellerId)
                    .orElseThrow(() -> new RuntimeException("No hay verificacion pendiente"));

    if (!"SELFIE_UPLOADED".equals(verification.getStatus().getValue())) {
      throw new RuntimeException("Primero sube la cedula y la selfie");
    }

    String selfieObject = verification.getSelfieMinioKey().getValue();
    String documentObject = verification.getDocumentMinioKey().getValue();

    FaceMatcherAdapter.FaceMatchResult result = faceMatcherAdapter.match(selfieObject, documentObject);

    boolean isApproved = result.match() && result.confidence() >= 0.6;

    if (isApproved) {
      verification =
              verification.approved(new SellerVerificationConfidenceScore(result.confidence()));
    } else {
      verification =
              verification.rejected(new SellerVerificationRejectionReason(result.message()));
    }

    SellerVerification savedVerification = repository.save(verification);

    eventPublisher.publishEvent(
            new SellerVerificationCompletedEvent(
                    sellerId,
                    isApproved,
                    result.confidence(),
                    result.message(),
                    Instant.now()));

    return savedVerification;
  }

  public SellerVerification getStatus(UUID sellerId) {
    return repository
            .findBySellerId(sellerId)
            .orElseThrow(() -> new RuntimeException("No hay verificacion para este vendedor"));
  }
}