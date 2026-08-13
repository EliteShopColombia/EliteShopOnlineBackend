package com.eliteshop.colombia.seller.infrastructure.controller.verification;

import com.eliteshop.colombia.seller.domain.model.verification.SellerVerification;
import org.springframework.stereotype.Component;

@Component
public class SellerVerificationMapperResponse {

    public SellerVerificationResponse toResponse(SellerVerification domain) {
        SellerVerificationResponse response = new SellerVerificationResponse();
        response.setId(domain.getId().getValue());
        response.setSellerId(domain.getSellerId().getValue());
        response.setVerificationType(domain.getVerificationType().getValue());
        response.setDocumentType(domain.getDocumentType().getValue());
        response.setStatus(domain.getStatus().getValue());
        response.setConfidenceScore(
                domain.getConfidenceScore() != null ? domain.getConfidenceScore().getValue() : null);
        response.setRejectionReason(
                domain.getRejectionReason() != null ? domain.getRejectionReason().getValue() : null);
        response.setCreatedAt(domain.getCreatedAt().getValue());
        response.setUpdatedAt(
                domain.getUpdatedAt() != null ? domain.getUpdatedAt().getValue() : null);
        return response;
    }
}