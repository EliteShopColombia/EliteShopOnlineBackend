package com.eliteshop.colombia.order.infrastructure.controller.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderRequest {

  @NotNull(message = "Customer ID is required")
  private UUID customerId;

  @NotNull(message = "Total amount is required")
  private BigDecimal totalAmount;

  @NotNull(message = "Shipping address is required")
  private String shippingAddress;

  @NotNull(message = "Shipping department is required")
  private String shippingDepartment;

  @NotNull(message = "Shipping city is required")
  private String shippingCity;

  private String status;
}
