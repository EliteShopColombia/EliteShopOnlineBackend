package com.eliteshop.colombia.seller.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerVerificationJpaRepository
    extends JpaRepository<SellerVerificationEntity, UUID> {

  Optional<SellerVerificationEntity> findBySellerId(UUID sellerId);
}
