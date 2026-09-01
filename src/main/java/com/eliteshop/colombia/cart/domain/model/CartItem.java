package com.eliteshop.colombia.cart.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class CartItem {
  @NonNull private final CartItemId id;
  @NonNull private final CartId cartId;
  @NonNull private final CartItemProductId productId;
  @NonNull private final CartItemQuantity quantity;
  @NonNull private final CartItemUnitPrice unitPrice;
  @NonNull private final LocalDateTime addedAt;

  public BigDecimal getSubtotal() {
    return unitPrice.getValue().multiply(BigDecimal.valueOf(quantity.getValue()));
  }

  public static CartItem create(
      CartId cartId,
      CartItemProductId productId,
      CartItemQuantity quantity,
      CartItemUnitPrice unitPrice) {
    return new CartItem(
        CartItemId.generate(), cartId, productId, quantity, unitPrice, LocalDateTime.now());
  }

  public CartItem withQuantity(CartItemQuantity newQuantity) {
    return new CartItem(
        this.id, this.cartId, this.productId, newQuantity, this.unitPrice, this.addedAt);
  }
}
