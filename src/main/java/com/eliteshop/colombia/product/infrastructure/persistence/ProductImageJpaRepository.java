package com.eliteshop.colombia.product.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageJpaRepository extends JpaRepository<ProductImageEntity, UUID> {
  List<ProductImageEntity> findByProductIdOrderByOrderAsc(UUID productId);

  void deleteByProductId(UUID productId);
}
