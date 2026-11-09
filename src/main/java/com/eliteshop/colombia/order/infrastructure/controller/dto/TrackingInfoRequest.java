package com.eliteshop.colombia.order.infrastructure.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrackingInfoRequest {

  @NotBlank(message = "El número de guía es requerido")
  private String trackingNumber;

  @NotBlank(message = "La transportadora es requerida")
  private String shippingCarrier;

  private String shippingLabelUrl;
}
