package com.eliteshop.colombia.cart.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartJpaRepository extends JpaRepository<CartEntity, UUID> {
  Optional<CartEntity> findByCustomerId(UUID customerId);

  void deleteByCustomerId(UUID customerId);
}
