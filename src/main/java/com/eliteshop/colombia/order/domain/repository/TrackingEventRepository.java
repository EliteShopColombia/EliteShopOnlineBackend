package com.eliteshop.colombia.order.domain.repository;

import com.eliteshop.colombia.order.domain.model.tracking.TrackingEvent;
import java.util.List;
import java.util.UUID;

public interface TrackingEventRepository {
  TrackingEvent save(TrackingEvent event);

  List<TrackingEvent> findByOrderId(UUID orderId);

  List<TrackingEvent> findAll();
}
