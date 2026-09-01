package com.eliteshop.colombia.order.infrastructure.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrackingInfoRequest {

  @NotBlank(message = "Tracking number is required")
  private String trackingNumber;

  @NotBlank(message = "Shipping carrier is required")
  private String shippingCarrier;

  private String shippingLabelUrl;
}
