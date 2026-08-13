package com.eliteshop.colombia.seller.infrastructure.mapper;

import com.eliteshop.colombia.seller.domain.model.verification.SellerVerification;
import com.eliteshop.colombia.seller.domain.model.verification.SellerVerificationConfidenceScore;
import com.eliteshop.colombia.seller.domain.model.verification.SellerVerificationCreatedAt;
import com.eliteshop.colombia.seller.domain.model.verification.SellerVerificationDocumentMinioKey;
import com.eliteshop.colombia.seller.domain.model.verification.SellerVerificationDocumentNumber;
import com.eliteshop.colombia.seller.domain.model.verification.SellerVerificationDocumentType;
import com.eliteshop.colombia.seller.domain.model.verification.SellerVerificationId;
import com.eliteshop.colombia.seller.domain.model.verification.SellerVerificationRejectionReason;
import com.eliteshop.colombia.seller.domain.model.verification.SellerVerificationSelfieMinioKey;
import com.eliteshop.colombia.seller.domain.model.verification.SellerVerificationSellerId;
import com.eliteshop.colombia.seller.domain.model.verification.SellerVerificationStatus;
import com.eliteshop.colombia.seller.domain.model.verification.SellerVerificationType;
import com.eliteshop.colombia.seller.domain.model.verification.SellerVerificationUpdatedAt;
import com.eliteshop.colombia.seller.infrastructure.persistence.SellerVerificationEntity;
import org.springframework.stereotype.Component;

@Component
public class SellerVerificationMapper {

  public SellerVerification toDomain(SellerVerificationEntity entity) {
    return new SellerVerification(
        new SellerVerificationId(entity.getId()),
        new SellerVerificationSellerId(entity.getSellerId()),
        new SellerVerificationType(entity.getVerificationType()),
        new SellerVerificationDocumentType(entity.getDocumentType()),
        entity.getDocumentNumber() != null
            ? new SellerVerificationDocumentNumber(entity.getDocumentNumber())
            : null,
        entity.getDocumentMinioKey() != null
            ? new SellerVerificationDocumentMinioKey(entity.getDocumentMinioKey())
            : null,
        entity.getSelfieMinioKey() != null
            ? new SellerVerificationSelfieMinioKey(entity.getSelfieMinioKey())
            : null,
        new SellerVerificationStatus(entity.getStatus()),
        entity.getConfidenceScore() != null
            ? new SellerVerificationConfidenceScore(entity.getConfidenceScore())
            : null,
        entity.getRejectionReason() != null
            ? new SellerVerificationRejectionReason(entity.getRejectionReason())
            : null,
        new SellerVerificationCreatedAt(entity.getCreatedAt()),
        entity.getUpdatedAt() != null
            ? new SellerVerificationUpdatedAt(entity.getUpdatedAt())
            : null);
  }

  public SellerVerificationEntity toEntity(SellerVerification domain) {
    SellerVerificationEntity entity = new SellerVerificationEntity();
    entity.setId(domain.getId().getValue());
    entity.setSellerId(domain.getSellerId().getValue());
    entity.setVerificationType(domain.getVerificationType().getValue());
    entity.setDocumentType(domain.getDocumentType().getValue());
    entity.setDocumentNumber(
        domain.getDocumentNumber() != null ? domain.getDocumentNumber().getValue() : null);
    entity.setDocumentMinioKey(
        domain.getDocumentMinioKey() != null ? domain.getDocumentMinioKey().getValue() : null);
    entity.setSelfieMinioKey(
        domain.getSelfieMinioKey() != null ? domain.getSelfieMinioKey().getValue() : null);
    entity.setStatus(domain.getStatus().getValue());
    entity.setConfidenceScore(
        domain.getConfidenceScore() != null ? domain.getConfidenceScore().getValue() : null);
    entity.setRejectionReason(
        domain.getRejectionReason() != null ? domain.getRejectionReason().getValue() : null);
    entity.setCreatedAt(domain.getCreatedAt().getValue());
    entity.setUpdatedAt(domain.getUpdatedAt() != null ? domain.getUpdatedAt().getValue() : null);
    return entity;
  }
}
