package com.eliteshop.colombia.cart.application;

import com.eliteshop.colombia.cart.domain.model.*;
import com.eliteshop.colombia.cart.domain.repository.CartRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ClearCartUseCase {

  private final CartRepository cartRepository;

  public void execute(UUID customerId) {
    log.info("Vaciando carrito del cliente {}", customerId);
    try {
      cartRepository.deleteByCustomerId(customerId);
      log.info("Carrito del cliente {} vaciado exitosamente", customerId);
    } catch (Exception e) {
      log.error("Error al vaciar carrito del cliente {}", customerId, e);
      throw e;
    }
  }
}
