package com.eliteshop.colombia.order.domain.model;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class OrderItemSellerId {
  private final UUID value;

  public OrderItemSellerId(UUID value) {
    Objects.requireNonNull(value, "El ID del vendedor no puede ser nulo");
    this.value = value;
  }
}
