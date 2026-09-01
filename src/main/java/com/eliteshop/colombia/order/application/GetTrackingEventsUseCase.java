package com.eliteshop.colombia.order.application;

import com.eliteshop.colombia.order.domain.model.OrderId;
import com.eliteshop.colombia.order.domain.model.tracking.TrackingEvent;
import com.eliteshop.colombia.order.domain.repository.TrackingEventRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class GetTrackingEventsUseCase {

  private final TrackingEventRepository trackingEventRepository;

  public List<TrackingEvent> execute(OrderId orderId) {
    log.info("Obteniendo eventos de tracking para orden {}", orderId);
    List<TrackingEvent> events = trackingEventRepository.findByOrderId(orderId.getValue());
    log.info("Se encontraron {} eventos de tracking para orden {}", events.size(), orderId);
    return events;
  }
}
