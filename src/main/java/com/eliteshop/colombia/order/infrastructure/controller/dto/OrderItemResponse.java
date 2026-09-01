package com.eliteshop.colombia.order.infrastructure.controller.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemResponse {

  private UUID id;
  private UUID productId;
  private UUID sellerId;
  private int quantity;
  private BigDecimal unitPrice;
  private BigDecimal subtotal;
}
