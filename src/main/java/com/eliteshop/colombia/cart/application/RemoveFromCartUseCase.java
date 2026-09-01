package com.eliteshop.colombia.cart.application;

import com.eliteshop.colombia.cart.domain.exception.CartNotFoundException;
import com.eliteshop.colombia.cart.domain.model.*;
import com.eliteshop.colombia.cart.domain.repository.CartRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class RemoveFromCartUseCase {

  private final CartRepository cartRepository;

  public Cart execute(UUID customerId, UUID itemId) {
    log.info("Eliminando item {} del carrito del cliente {}", itemId, customerId);
    try {
      Cart cart =
          cartRepository
              .findByCustomerId(customerId)
              .orElseThrow(() -> new CartNotFoundException("Carrito no encontrado"));

      cart.removeItem(new CartItemId(itemId));

      Cart savedCart = cartRepository.save(cart);
      log.info("Item {} eliminado exitosamente del carrito del cliente {}", itemId, customerId);
      return savedCart;
    } catch (Exception e) {
      log.error("Error al eliminar item {} del carrito del cliente {}", itemId, customerId, e);
      throw e;
    }
  }
}
