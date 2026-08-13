package com.eliteshop.colombia.seller.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerJpaRepository extends JpaRepository<SellerEntity, UUID> {
  Optional<SellerEntity> findByDniNumber(String dniNumber);
}
