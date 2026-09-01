package com.eliteshop.colombia.order.domain.model.tracking;

import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class TrackingEventId {
  private final UUID value;

  public static TrackingEventId generate() {
    return new TrackingEventId(UUID.randomUUID());
  }
}
