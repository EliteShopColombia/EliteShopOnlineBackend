package com.eliteshop.colombia.order.infrastructure.controller.dto;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryResponse {

  private UUID sellerId;
  private int totalOrders;
  private BigDecimal totalRevenue;
  private Map<String, Integer> ordersByStatus;
  private BigDecimal averageOrderValue;
}
