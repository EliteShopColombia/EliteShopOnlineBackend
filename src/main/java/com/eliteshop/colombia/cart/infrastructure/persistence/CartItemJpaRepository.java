package com.eliteshop.colombia.cart.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemJpaRepository extends JpaRepository<CartItemEntity, UUID> {
  List<CartItemEntity> findByCartId(UUID cartId);

  void deleteByCartId(UUID cartId);

  /**
   * Bloquea las filas del carrito (SELECT ... FOR UPDATE) para serializar escrituras
   * concurrentes sobre el mismo carrito y evitar actualizaciones perdidas.
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT i FROM CartItemEntity i WHERE i.cartId = :cartId")
  List<CartItemEntity> lockByCartId(@Param("cartId") UUID cartId);
}
