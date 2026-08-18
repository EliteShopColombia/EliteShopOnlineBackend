package com.eliteshop.colombia.order.domain.model;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class OrderTotalAmount {
  private final BigDecimal value;
}
