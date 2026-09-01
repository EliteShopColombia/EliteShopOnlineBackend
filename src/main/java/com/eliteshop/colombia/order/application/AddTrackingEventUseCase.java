package com.eliteshop.colombia.order.application;

import com.eliteshop.colombia.order.domain.exception.OrderNotFoundException;
import com.eliteshop.colombia.order.domain.model.OrderId;
import com.eliteshop.colombia.order.domain.model.tracking.TrackingEvent;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import com.eliteshop.colombia.order.domain.repository.TrackingEventRepository;
import java.sql.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class AddTrackingEventUseCase {

  private final TrackingEventRepository trackingEventRepository;
  private final OrderRepository orderRepository;

  public TrackingEvent execute(
      OrderId orderId,
      String status,
      String location,
      String description,
      Timestamp eventTimestamp) {
    log.info("Agregando evento de tracking para orden {}", orderId);

    if (orderRepository.findById(orderId).isEmpty()) {
      log.error("Orden no encontrada con id: {}", orderId);
      throw new OrderNotFoundException("The order not exist in our platform");
    }

    TrackingEvent event =
        TrackingEvent.create(orderId.getValue(), status, location, description, eventTimestamp);
    TrackingEvent saved = trackingEventRepository.save(event);

    log.info("Evento de tracking agregado para orden {}: {}", orderId, status);
    return saved;
  }
}
