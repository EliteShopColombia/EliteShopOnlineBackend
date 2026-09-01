package com.eliteshop.colombia.cart.infrastructure.controller;

import com.eliteshop.colombia.cart.application.*;
import com.eliteshop.colombia.cart.domain.model.Cart;
import com.eliteshop.colombia.cart.infrastructure.controller.dto.AddToCartRequest;
import com.eliteshop.colombia.cart.infrastructure.controller.dto.CartResponse;
import com.eliteshop.colombia.cart.infrastructure.controller.dto.UpdateCartItemRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

  private final GetCartUseCase getCartUseCase;
  private final AddToCartUseCase addToCartUseCase;
  private final UpdateCartItemUseCase updateCartItemUseCase;
  private final RemoveFromCartUseCase removeFromCartUseCase;
  private final ClearCartUseCase clearCartUseCase;

  @GetMapping
  public ResponseEntity<CartResponse> getCart(HttpServletRequest request) {
    UUID customerId = currentCustomerId(request);
    Cart cart = getCartUseCase.execute(customerId);
    return ResponseEntity.ok(toResponse(cart));
  }

  @PostMapping("/items")
  public ResponseEntity<CartResponse> addItem(
      HttpServletRequest request, @Valid @RequestBody AddToCartRequest addToCartRequest) {
    UUID customerId = currentCustomerId(request);
    Cart cart =
        addToCartUseCase.execute(
            customerId, addToCartRequest.getProductId(), addToCartRequest.getQuantity());
    return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(cart));
  }

  @PutMapping("/items/{itemId}")
  public ResponseEntity<CartResponse> updateItem(
      HttpServletRequest request,
      @PathVariable UUID itemId,
      @Valid @RequestBody UpdateCartItemRequest updateCartItemRequest) {
    UUID customerId = currentCustomerId(request);
    Cart cart =
        updateCartItemUseCase.execute(customerId, itemId, updateCartItemRequest.getQuantity());
    return ResponseEntity.ok(toResponse(cart));
  }

  @DeleteMapping("/items/{itemId}")
  public ResponseEntity<CartResponse> removeItem(
      HttpServletRequest request, @PathVariable UUID itemId) {
    UUID customerId = currentCustomerId(request);
    Cart cart = removeFromCartUseCase.execute(customerId, itemId);
    return ResponseEntity.ok(toResponse(cart));
  }

  @DeleteMapping
  public ResponseEntity<Void> clearCart(HttpServletRequest request) {
    UUID customerId = currentCustomerId(request);
    clearCartUseCase.execute(customerId);
    return ResponseEntity.noContent().build();
  }

  private UUID currentCustomerId(HttpServletRequest request) {
    return UUID.fromString((String) request.getAttribute("gateway.userId"));
  }

  private CartResponse toResponse(Cart cart) {
    CartResponse response = new CartResponse();
    response.setId(cart.getId().getValue());
    response.setTotal(cart.getTotal());
    response.setItemCount(cart.getItemCount());
    response.setItems(
        cart.getItems().stream()
            .map(
                item -> {
                  CartResponse.CartItemResponse itemResponse = new CartResponse.CartItemResponse();
                  itemResponse.setId(item.getId().getValue());
                  itemResponse.setProductId(item.getProductId().getValue());
                  itemResponse.setQuantity(item.getQuantity().getValue());
                  itemResponse.setUnitPrice(item.getUnitPrice().getValue());
                  itemResponse.setSubtotal(item.getSubtotal());
                  return itemResponse;
                })
            .collect(Collectors.toList()));
    return response;
  }
}
