package com.eliteshop.colombia.cart.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemJpaRepository extends JpaRepository<CartItemEntity, UUID> {
  List<CartItemEntity> findByCartId(UUID cartId);

  void deleteByCartId(UUID cartId);
}
