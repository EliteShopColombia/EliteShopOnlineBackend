package com.eliteshop.colombia.cart.application;

import com.eliteshop.colombia.cart.domain.model.*;
import com.eliteshop.colombia.cart.domain.repository.CartRepository;
import com.eliteshop.colombia.product.domain.exception.ProductNotFoundException;
import com.eliteshop.colombia.product.domain.model.Product;
import com.eliteshop.colombia.product.domain.model.ProductId;
import com.eliteshop.colombia.product.domain.repository.ProductRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class AddToCartUseCase {

  private final CartRepository cartRepository;
  private final ProductRepository productRepository;

  public Cart execute(UUID customerId, UUID productId, int quantity) {
    log.info(
        "Agregando producto {} al carrito del cliente {} cantidad={}",
        productId,
        customerId,
        quantity);
    try {
      Product product =
          productRepository
              .findById(new ProductId(productId))
              .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado"));

      if (product.getStock().getValue() < quantity) {
        throw new IllegalArgumentException("Stock insuficiente");
      }

      Cart cart =
          cartRepository.findByCustomerId(customerId).orElseGet(() -> Cart.create(customerId));

      Optional<CartItem> existingItem = cart.findItemByProductId(productId);

      if (existingItem.isPresent()) {
        int newQuantity = existingItem.get().getQuantity().getValue() + quantity;
        if (newQuantity > product.getStock().getValue()) {
          throw new IllegalArgumentException("Stock insuficiente");
        }
        cart.updateItemQuantity(existingItem.get().getId(), newQuantity);
      } else {
        cart.addItem(
            new CartItemProductId(productId),
            new CartItemQuantity(quantity),
            new CartItemUnitPrice(product.getPrice().getValue()));
      }

      Cart savedCart = cartRepository.save(cart);
      log.info(
          "Producto {} agregado exitosamente al carrito del cliente {}", productId, customerId);
      return savedCart;
    } catch (Exception e) {
      log.error("Error al agregar producto {} al carrito del cliente {}", productId, customerId, e);
      throw e;
    }
  }
}
