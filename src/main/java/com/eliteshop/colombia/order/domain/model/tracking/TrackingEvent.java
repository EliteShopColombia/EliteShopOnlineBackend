package com.eliteshop.colombia.order.domain.model.tracking;

import java.sql.Timestamp;
import java.util.UUID;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class TrackingEvent {
  @NonNull private final TrackingEventId id;
  @NonNull private final TrackingEventOrderId orderId;
  @NonNull private final TrackingEventStatus status;
  private final String location;
  private final String description;
  @NonNull private final Timestamp eventTimestamp;
  @NonNull private final Timestamp createdAt;

  public static TrackingEvent create(
      UUID orderId, String status, String location, String description, Timestamp eventTimestamp) {
    return new TrackingEvent(
        TrackingEventId.generate(),
        new TrackingEventOrderId(orderId),
        TrackingEventStatus.fromValue(status),
        location,
        description,
        eventTimestamp,
        new Timestamp(System.currentTimeMillis()));
  }
}
