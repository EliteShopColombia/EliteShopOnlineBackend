package com.eliteshop.colombia.cart.application;

import com.eliteshop.colombia.cart.domain.exception.CartItemNotFoundException;
import com.eliteshop.colombia.cart.domain.exception.CartNotFoundException;
import com.eliteshop.colombia.cart.domain.model.*;
import com.eliteshop.colombia.cart.domain.repository.CartRepository;
import com.eliteshop.colombia.product.domain.model.ProductId;
import com.eliteshop.colombia.product.domain.repository.ProductRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class UpdateCartItemUseCase {

  private final CartRepository cartRepository;
  private final ProductRepository productRepository;

  public Cart execute(UUID customerId, UUID itemId, int newQuantity) {
    log.info(
        "Actualizando item {} del carrito del cliente {} nuevaCantidad={}",
        itemId,
        customerId,
        newQuantity);
    try {
      Cart cart =
          cartRepository
              .findByCustomerId(customerId)
              .orElseThrow(() -> new CartNotFoundException("Carrito no encontrado"));

      CartItem item =
          cart.getItems().stream()
              .filter(i -> i.getId().getValue().equals(itemId))
              .findFirst()
              .orElseThrow(() -> new CartItemNotFoundException("Item no encontrado en el carrito"));

      var product =
          productRepository
              .findById(new ProductId(item.getProductId().getValue()))
              .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

      if (product.getStock().getValue() < newQuantity) {
        throw new IllegalArgumentException("Stock insuficiente");
      }

      cart.updateItemQuantity(item.getId(), newQuantity);

      Cart savedCart = cartRepository.save(cart);
      log.info("Item {} actualizado exitosamente en carrito del cliente {}", itemId, customerId);
      return savedCart;
    } catch (Exception e) {
      log.error("Error al actualizar item {} del carrito del cliente {}", itemId, customerId, e);
      throw e;
    }
  }
}
