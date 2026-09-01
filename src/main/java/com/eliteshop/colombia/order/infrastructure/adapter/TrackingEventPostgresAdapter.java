package com.eliteshop.colombia.order.infrastructure.adapter;

import com.eliteshop.colombia.order.domain.model.tracking.TrackingEvent;
import com.eliteshop.colombia.order.domain.repository.TrackingEventRepository;
import com.eliteshop.colombia.order.infrastructure.persistence.tracking.TrackingEventEntity;
import com.eliteshop.colombia.order.infrastructure.persistence.tracking.TrackingEventJpaRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrackingEventPostgresAdapter implements TrackingEventRepository {

  private final TrackingEventJpaRepository jpaRepository;

  @Override
  public TrackingEvent save(TrackingEvent event) {
    TrackingEventEntity entity = toEntity(event);
    TrackingEventEntity savedEntity = jpaRepository.save(entity);
    return toDomain(savedEntity);
  }

  @Override
  public List<TrackingEvent> findByOrderId(UUID orderId) {
    return jpaRepository.findByOrderIdOrderByEventTimestampAsc(orderId).stream()
        .map(this::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<TrackingEvent> findAll() {
    return jpaRepository.findAll().stream().map(this::toDomain).collect(Collectors.toList());
  }

  private TrackingEventEntity toEntity(TrackingEvent domain) {
    TrackingEventEntity entity = new TrackingEventEntity();
    entity.setId(domain.getId().getValue());
    entity.setOrderId(domain.getOrderId().getValue());
    entity.setStatus(domain.getStatus().name());
    entity.setLocation(domain.getLocation());
    entity.setDescription(domain.getDescription());
    entity.setEventTimestamp(domain.getEventTimestamp());
    entity.setCreatedAt(domain.getCreatedAt());
    return entity;
  }

  private TrackingEvent toDomain(TrackingEventEntity entity) {
    return new TrackingEvent(
        new com.eliteshop.colombia.order.domain.model.tracking.TrackingEventId(entity.getId()),
        new com.eliteshop.colombia.order.domain.model.tracking.TrackingEventOrderId(
            entity.getOrderId()),
        com.eliteshop.colombia.order.domain.model.tracking.TrackingEventStatus.fromValue(
            entity.getStatus()),
        entity.getLocation(),
        entity.getDescription(),
        entity.getEventTimestamp(),
        entity.getCreatedAt());
  }
}
