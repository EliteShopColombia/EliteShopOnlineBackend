package com.eliteshop.colombia.product.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, UUID> {
  Optional<ProductEntity> findByName(String name);

  @Modifying
  @Transactional
  @Query(
      "UPDATE ProductEntity p SET p.stock = p.stock - :quantity"
          + " WHERE p.id = :productId AND p.stock >= :quantity")
  int reduceStock(@Param("productId") UUID productId, @Param("quantity") int quantity);

  @Modifying
  @Transactional
  @Query("UPDATE ProductEntity p SET p.stock = p.stock + :quantity WHERE p.id = :productId")
  int restoreStock(@Param("productId") UUID productId, @Param("quantity") int quantity);
}
