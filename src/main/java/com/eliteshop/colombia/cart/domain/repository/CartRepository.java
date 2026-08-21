package com.eliteshop.colombia.cart.domain.repository;

import com.eliteshop.colombia.cart.domain.model.Cart;
import com.eliteshop.colombia.cart.domain.model.CartId;
import java.util.Optional;
import java.util.UUID;

public interface CartRepository {
  Cart save(Cart cart);

  Optional<Cart> findById(CartId id);

  Optional<Cart> findByCustomerId(UUID customerId);

  void deleteById(CartId id);

  void deleteByCustomerId(UUID customerId);
}
