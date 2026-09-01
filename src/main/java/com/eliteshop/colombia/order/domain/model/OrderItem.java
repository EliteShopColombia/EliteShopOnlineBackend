package com.eliteshop.colombia.order.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class OrderItem {
  @NonNull private final OrderItemId id;
  @NonNull private final OrderItemOrderId orderId;
  @NonNull private final OrderItemProductId productId;
  @NonNull private final OrderItemSellerId sellerId;
  @NonNull private final OrderItemQuantity quantity;
  @NonNull private final OrderItemUnitPrice unitPrice;
  @NonNull private final LocalDateTime createdAt;

  public BigDecimal getSubtotal() {
    return unitPrice.getValue().multiply(BigDecimal.valueOf(quantity.getValue()));
  }

  public static OrderItem create(
      UUID orderId, UUID productId, UUID sellerId, int quantity, BigDecimal unitPrice) {
    return new OrderItem(
        OrderItemId.generate(),
        new OrderItemOrderId(orderId),
        new OrderItemProductId(productId),
        new OrderItemSellerId(sellerId),
        new OrderItemQuantity(quantity),
        new OrderItemUnitPrice(unitPrice),
        LocalDateTime.now());
  }
}
