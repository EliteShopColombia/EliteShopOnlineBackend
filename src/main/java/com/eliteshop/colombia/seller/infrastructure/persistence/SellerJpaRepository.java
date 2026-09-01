package com.eliteshop.colombia.seller.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SellerJpaRepository extends JpaRepository<SellerEntity, UUID> {
  Optional<SellerEntity> findByDniNumber(String dniNumber);

  Optional<SellerEntity> findByContactEmail(String email);

  @EntityGraph(attributePaths = {"contact", "bankInfo"})
  @Query("SELECT s FROM SellerEntity s")
  Page<SellerEntity> findAllWithDetails(Pageable pageable);

  @EntityGraph(attributePaths = {"contact", "bankInfo"})
  @Query("SELECT s FROM SellerEntity s WHERE s.id = :id")
  Optional<SellerEntity> findByIdWithDetails(@Param("id") UUID id);
}
