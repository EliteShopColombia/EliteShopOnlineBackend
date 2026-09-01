package com.eliteshop.colombia.order.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "orders")
public class OrderEntity {

  @Id
  @Column(name = "order_id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "customer_id", nullable = false)
  private UUID customerId;

  @Column(name = "order_status", nullable = false)
  private String status;

  @Column(name = "total_amount", nullable = false)
  private java.math.BigDecimal totalAmount;

  @Column(name = "shipping_address", nullable = false)
  private String shippingAddress;

  @Column(name = "shipping_department", nullable = false)
  private String shippingDepartment;

  @Column(name = "shipping_city", nullable = false)
  private String shippingCity;

  @Column(name = "created_at")
  private Timestamp createdAt;

  @Column(name = "updated_at")
  private Timestamp updatedAt;

  @Column(name = "tracking_number")
  private String trackingNumber;

  @Column(name = "shipping_carrier")
  private String shippingCarrier;

  @Column(name = "shipping_label_url")
  private String shippingLabelUrl;

  @Column(name = "dispute_reason")
  private String disputeReason;
}
