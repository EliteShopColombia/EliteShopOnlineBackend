package com.eliteshop.colombia.product.domain.model;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ProductPrice {
  private final BigDecimal value;
}
