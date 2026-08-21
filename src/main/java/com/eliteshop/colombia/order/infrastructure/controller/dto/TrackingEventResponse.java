package com.eliteshop.colombia.order.infrastructure.controller.dto;

import java.sql.Timestamp;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrackingEventResponse {

  private UUID id;
  private UUID orderId;
  private String status;
  private String location;
  private String description;
  private Timestamp eventTimestamp;
  private Timestamp createdAt;
}
