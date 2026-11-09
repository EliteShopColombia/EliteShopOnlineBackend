package com.eliteshop.colombia.order.infrastructure.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrackingEventRequest {

  @NotBlank(message = "El estado es requerido")
  private String status;

  private String location;

  private String description;

  @NotNull(message = "La marca de tiempo del evento es requerida")
  private Timestamp eventTimestamp;
}
