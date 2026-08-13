package com.eliteshop.colombia.seller.infrastructure.controller.verification;

import com.eliteshop.colombia.seller.domain.model.verification.SellerVerification;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SellerVerificationResponse {
    private UUID id;
    private UUID sellerId;
    private String verificationType;
    private String documentType;
    private String status;
    private Double confidenceScore;
    private String rejectionReason;
    private Instant createdAt;
    private Instant updatedAt;
}