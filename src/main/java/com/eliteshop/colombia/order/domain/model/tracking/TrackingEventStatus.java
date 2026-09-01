package com.eliteshop.colombia.order.domain.model.tracking;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TrackingEventStatus {
  RECEIVED("Recibido"),
  IN_TRANSIT("En transito"),
  OUT_FOR_DELIVERY("En reparto"),
  DELIVERED("Entregado"),
  FAILED("Fallido");

  private final String description;

  public static TrackingEventStatus fromValue(String value) {
    try {
      return valueOf(value.toUpperCase());
    } catch (IllegalArgumentException e) {
      return RECEIVED;
    }
  }
}
