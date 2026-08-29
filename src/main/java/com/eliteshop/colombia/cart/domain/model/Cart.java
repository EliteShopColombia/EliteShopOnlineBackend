package com.eliteshop.colombia.cart.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class Cart {
  @NonNull private final CartId id;
  @NonNull private final CartCustomerId customerId;
  private final List<CartItem> items;
  @NonNull private final LocalDateTime createdAt;
  private final LocalDateTime updatedAt;

  public Cart(CartId id, CartCustomerId customerId) {
    this(id, customerId, new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now());
  }

  public List<CartItem> getItems() {
    return items != null ? Collections.unmodifiableList(items) : Collections.emptyList();
  }

  public BigDecimal getTotal() {
    return getItems().stream().map(CartItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public int getItemCount() {
    return getItems().stream().map(item -> item.getQuantity().getValue()).reduce(0, Integer::sum);
  }

  public Optional<CartItem> findItemByProductId(UUID productId) {
    return getItems().stream()
        .filter(item -> item.getProductId().getValue().equals(productId))
        .findFirst();
  }

  public void addItem(
      CartItemProductId productId, CartItemQuantity quantity, CartItemUnitPrice unitPrice) {
    Optional<CartItem> existing = findItemByProductId(productId.getValue());
    if (existing.isPresent()) {
      throw new com.eliteshop.colombia.cart.domain.exception.CartItemAlreadyExistsException(
          "El producto ya esta en el carrito, usa updateItemQuantity");
    }
    ((ArrayList<CartItem>) this.items)
        .add(CartItem.create(this.id, productId, quantity, unitPrice));
  }

  public void updateItemQuantity(CartItemId itemId, int newQuantity) {
    ArrayList<CartItem> mutableItems = (ArrayList<CartItem>) this.items;
    for (int i = 0; i < mutableItems.size(); i++) {
      CartItem item = mutableItems.get(i);
      if (item.getId().getValue().equals(itemId.getValue())) {
        mutableItems.set(i, item.withQuantity(new CartItemQuantity(newQuantity)));
        return;
      }
    }
    throw new com.eliteshop.colombia.cart.domain.exception.CartItemNotFoundException(
        "Item no encontrado en el carrito");
  }

  public void removeItem(CartItemId itemId) {
    ((ArrayList<CartItem>) this.items)
        .removeIf(item -> item.getId().getValue().equals(itemId.getValue()));
  }

  public void clear() {
    ((ArrayList<CartItem>) this.items).clear();
  }

  public static Cart create(UUID customerId) {
    return new Cart(CartId.generate(), new CartCustomerId(customerId));
  }
}
