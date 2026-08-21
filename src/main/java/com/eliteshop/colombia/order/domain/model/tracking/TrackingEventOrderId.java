package com.eliteshop.colombia.order.domain.model.tracking;

import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class TrackingEventOrderId {
  private final UUID value;
}
