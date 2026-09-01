package com.eliteshop.colombia.order.infrastructure.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrackingEventRequest {

  @NotBlank(message = "Status is required")
  private String status;

  private String location;

  private String description;

  @NotNull(message = "Event timestamp is required")
  private Timestamp eventTimestamp;
}
