package com.eliteshop.colombia.order.infrastructure.adapter;

import com.eliteshop.colombia.order.domain.model.Order;
import com.eliteshop.colombia.order.domain.model.OrderId;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import com.eliteshop.colombia.order.infrastructure.mapper.OrderMapper;
import com.eliteshop.colombia.order.infrastructure.persistence.OrderJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderPostgresAdapter implements OrderRepository {

  private final OrderJpaRepository jpaRepository;
  private final OrderMapper mapper;

  @Override
  public Order save(Order order) {
    var entity = mapper.toEntity(order);
    var savedEntity = jpaRepository.save(entity);
    return mapper.toDomain(savedEntity);
  }

  @Override
  public void update(Order order) {
    var existingEntity = jpaRepository.findById(order.getId().getValue()).orElseThrow();

    existingEntity.setCustomerId(order.getCustomerId().getValue());
    existingEntity.setStatus(order.getStatus().name());
    existingEntity.setTotalAmount(order.getTotalAmount().getValue());
    existingEntity.setShippingAddress(order.getShippingAddress().getValue());
    existingEntity.setShippingDepartment(order.getShippingDepartment().getValue());
    existingEntity.setShippingCity(order.getShippingCity().getValue());
    if (order.getUpdatedAt() != null) {
      existingEntity.setUpdatedAt(order.getUpdatedAt().getValue());
    }
    existingEntity.setTrackingNumber(order.getTrackingNumber());
    existingEntity.setShippingCarrier(order.getShippingCarrier());
    existingEntity.setShippingLabelUrl(order.getShippingLabelUrl());

    jpaRepository.save(existingEntity);
  }

  @Override
  public void delete(OrderId id) {
    jpaRepository.deleteById(id.getValue());
  }

  @Override
  public List<Order> findAll() {
    return jpaRepository.findAll().stream().map(mapper::toDomain).collect(Collectors.toList());
  }

  @Override
  public Optional<Order> findById(OrderId id) {
    return jpaRepository.findById(id.getValue()).map(mapper::toDomain);
  }

  @Override
  public List<Order> findByCustomerId(UUID customerId) {
    return jpaRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
        .map(mapper::toDomain)
        .collect(Collectors.toList());
  }
}
