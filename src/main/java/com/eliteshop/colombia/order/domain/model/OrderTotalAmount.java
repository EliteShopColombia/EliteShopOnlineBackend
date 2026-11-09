package com.eliteshop.colombia.order.domain.model;

import java.math.BigDecimal;
import java.util.Objects;
import lombok.Getter;

@Getter
public class OrderTotalAmount {
  private final BigDecimal value;

  public OrderTotalAmount(BigDecimal value) {
    Objects.requireNonNull(value, "Monto total no puede ser null");
    if (value.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("El monto total debe ser mayor que cero: " + value);
    }
    this.value = value;
  }
}
