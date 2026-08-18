package com.eliteshop.colombia.order.infrastructure.mapper;

import com.eliteshop.colombia.order.domain.model.Order;
import com.eliteshop.colombia.order.domain.model.OrderCreatedAt;
import com.eliteshop.colombia.order.domain.model.OrderCustomerId;
import com.eliteshop.colombia.order.domain.model.OrderId;
import com.eliteshop.colombia.order.domain.model.OrderShippingAddress;
import com.eliteshop.colombia.order.domain.model.OrderShippingCity;
import com.eliteshop.colombia.order.domain.model.OrderShippingDepartment;
import com.eliteshop.colombia.order.domain.model.OrderStatus;
import com.eliteshop.colombia.order.domain.model.OrderTotalAmount;
import com.eliteshop.colombia.order.domain.model.OrderUpdatedAt;
import com.eliteshop.colombia.order.infrastructure.controller.dto.OrderRequest;
import com.eliteshop.colombia.order.infrastructure.controller.dto.OrderResponse;
import com.eliteshop.colombia.order.infrastructure.persistence.OrderEntity;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

  public Order toDomain(OrderEntity entity) {
    if (entity == null) {
      return null;
    }
    return new Order(
        new OrderId(entity.getId()),
        new OrderCustomerId(entity.getCustomerId()),
        OrderStatus.valueOf(entity.getStatus()),
        new OrderTotalAmount(entity.getTotalAmount()),
        new OrderShippingAddress(entity.getShippingAddress()),
        new OrderShippingDepartment(entity.getShippingDepartment()),
        new OrderShippingCity(entity.getShippingCity()),
        new OrderCreatedAt(entity.getCreatedAt()),
        entity.getUpdatedAt() != null ? new OrderUpdatedAt(entity.getUpdatedAt()) : null);
  }

  public OrderEntity toEntity(Order domain) {
    if (domain == null) {
      return null;
    }
    OrderEntity entity = new OrderEntity();
    entity.setId(domain.getId().getValue());
    entity.setCustomerId(domain.getCustomerId().getValue());
    entity.setStatus(domain.getStatus().name());
    entity.setTotalAmount(domain.getTotalAmount().getValue());
    entity.setShippingAddress(domain.getShippingAddress().getValue());
    entity.setShippingDepartment(domain.getShippingDepartment().getValue());
    entity.setShippingCity(domain.getShippingCity().getValue());
    entity.setCreatedAt(domain.getCreatedAt().getValue());
    if (domain.getUpdatedAt() != null) {
      entity.setUpdatedAt(domain.getUpdatedAt().getValue());
    }
    return entity;
  }

  public Order toDomainFromRequest(OrderRequest request) {
    if (request == null) {
      return null;
    }
    return new Order(
        new OrderId(UUID.randomUUID()),
        new OrderCustomerId(request.getCustomerId()),
        OrderStatus.PENDING,
        new OrderTotalAmount(request.getTotalAmount()),
        new OrderShippingAddress(request.getShippingAddress()),
        new OrderShippingDepartment(request.getShippingDepartment()),
        new OrderShippingCity(request.getShippingCity()),
        new OrderCreatedAt(Timestamp.from(Instant.now())),
        null);
  }

  public OrderResponse toResponse(Order domain) {
    if (domain == null) {
      return null;
    }
    OrderResponse response = new OrderResponse();
    response.setId(domain.getId().getValue());
    response.setCustomerId(domain.getCustomerId().getValue());
    response.setStatus(domain.getStatus().name());
    response.setTotalAmount(domain.getTotalAmount().getValue());
    response.setShippingAddress(domain.getShippingAddress().getValue());
    response.setShippingDepartment(domain.getShippingDepartment().getValue());
    response.setShippingCity(domain.getShippingCity().getValue());
    response.setCreatedAt(domain.getCreatedAt().getValue());
    if (domain.getUpdatedAt() != null) {
      response.setUpdatedAt(domain.getUpdatedAt().getValue());
    }
    return response;
  }
}
