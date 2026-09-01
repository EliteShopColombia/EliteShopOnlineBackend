package com.eliteshop.colombia.cart.application;

import com.eliteshop.colombia.cart.domain.model.*;
import com.eliteshop.colombia.cart.domain.repository.CartRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class GetCartUseCase {

  private final CartRepository cartRepository;

  public Cart execute(UUID customerId) {
    log.info("Consultando carrito del cliente {}", customerId);
    try {
      Cart cart =
          cartRepository.findByCustomerId(customerId).orElseGet(() -> Cart.create(customerId));
      log.info(
          "Carrito del cliente {} consultado exitosamente con {} items",
          customerId,
          cart.getItems().size());
      return cart;
    } catch (Exception e) {
      log.error("Error al consultar carrito del cliente {}", customerId, e);
      throw e;
    }
  }
}
