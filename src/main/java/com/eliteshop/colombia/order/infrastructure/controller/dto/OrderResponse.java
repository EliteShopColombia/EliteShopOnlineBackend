package com.eliteshop.colombia.order.infrastructure.controller.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderResponse {

  private UUID id;
  private UUID customerId;
  private String status;
  private BigDecimal totalAmount;
  private String shippingAddress;
  private String shippingDepartment;
  private String shippingCity;
  private Timestamp createdAt;
  private Timestamp updatedAt;
  private List<OrderItemResponse> items;
  private String trackingNumber;
  private String shippingCarrier;
  private String shippingLabelUrl;
  private String disputeReason;
}
