package com.eliteshop.colombia.cart.infrastructure.controller.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartResponse {

  private UUID id;
  private List<CartItemResponse> items;
  private BigDecimal total;
  private Integer itemCount;

  @Getter
  @Setter
  public static class CartItemResponse {
    private UUID id;
    private UUID productId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
  }
}
