package com.eliteshop.colombia.order.infrastructure.adapter;

import com.eliteshop.colombia.order.domain.exception.OrderNotFoundException;
import com.eliteshop.colombia.order.domain.model.DisputeReason;
import com.eliteshop.colombia.order.domain.model.Order;
import com.eliteshop.colombia.order.domain.model.OrderId;
import com.eliteshop.colombia.order.domain.model.OrderStatus;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import com.eliteshop.colombia.order.infrastructure.mapper.OrderMapper;
import com.eliteshop.colombia.order.infrastructure.persistence.OrderJpaRepository;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    var existingEntity =
        jpaRepository
            .findById(order.getId().getValue())
            .orElseThrow(() -> new OrderNotFoundException("Orden no encontrada"));

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
  public boolean updateStatusIfCurrent(
      OrderId orderId, OrderStatus currentStatus, OrderStatus newStatus, Timestamp updatedAt) {
    return jpaRepository.updateStatusIfCurrent(
            orderId.getValue(), currentStatus.name(), newStatus.name(), updatedAt)
        == 1;
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

  @Override
  public com.eliteshop.colombia.shared.domain.PageResult<Order> findPageByCustomerId(
      UUID customerId, int page, int size) {
    var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    var result = jpaRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable);
    List<Order> orders =
        result.getContent().stream().map(mapper::toDomain).collect(Collectors.toList());
    return com.eliteshop.colombia.shared.domain.PageResult.of(
        orders, page, size, result.getTotalElements());
  }

  @Override
  public com.eliteshop.colombia.shared.domain.PageResult<Order> findPageBySellerId(
      UUID sellerId, int page, int size) {
    var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    var result = jpaRepository.findPageBySellerId(sellerId, pageable);
    List<Order> orders =
        result.getContent().stream().map(mapper::toDomain).collect(Collectors.toList());
    return com.eliteshop.colombia.shared.domain.PageResult.of(
        orders, page, size, result.getTotalElements());
  }

  @Override
  public List<Order> findAllByIds(List<UUID> ids) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    return jpaRepository.findAllById(ids).stream()
        .map(mapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public void updateDisputeReason(OrderId orderId, DisputeReason reason) {
    var entity =
        jpaRepository
            .findById(orderId.getValue())
            .orElseThrow(() -> new OrderNotFoundException("Orden no encontrada"));
    entity.setDisputeReason(reason.name());
    jpaRepository.save(entity);
  }
}
