package com.eliteshop.colombia.order.domain.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class Order {
  @NonNull private final OrderId id;
  @NonNull private final OrderCustomerId customerId;
  @NonNull private final OrderStatus status;
  @NonNull private final OrderTotalAmount totalAmount;
  @NonNull private final OrderShippingAddress shippingAddress;
  @NonNull private final OrderShippingDepartment shippingDepartment;
  @NonNull private final OrderShippingCity shippingCity;
  @NonNull private final OrderCreatedAt createdAt;
  private final OrderUpdatedAt updatedAt;
  private final String trackingNumber;
  private final String shippingCarrier;
  private final String shippingLabelUrl;
  private final DisputeReason disputeReason;
}
