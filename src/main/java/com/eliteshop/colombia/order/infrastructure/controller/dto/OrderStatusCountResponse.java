package com.eliteshop.colombia.order.infrastructure.controller.dto;

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
public class OrderStatusCountResponse {

  private UUID sellerId;
  private int totalOrders;
  private Map<String, Integer> counts;
}
