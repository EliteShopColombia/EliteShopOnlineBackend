package com.eliteshop.colombia.seller.domain.model.verification;

import java.util.Optional;
import java.util.UUID;

public interface SellerVerificationRepository {
  SellerVerification save(SellerVerification verification);

  Optional<SellerVerification> findById(SellerVerificationId id);

  Optional<SellerVerification> findBySellerId(UUID sellerId);
}
