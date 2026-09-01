package com.eliteshop.colombia.admin.infrastructure.controller.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminDashboardResponse {
  private long totalCustomers;
  private long totalSellers;
  private long activeSellers;
  private long totalOrders;
  private BigDecimal totalRevenue;
  private long totalProducts;
}
