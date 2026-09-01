package com.eliteshop.colombia.order.infrastructure.mapper;

import com.eliteshop.colombia.order.domain.model.DisputeReason;
import com.eliteshop.colombia.order.domain.model.Order;
import com.eliteshop.colombia.order.domain.model.OrderCreatedAt;
import com.eliteshop.colombia.order.domain.model.OrderCustomerId;
import com.eliteshop.colombia.order.domain.model.OrderId;
import com.eliteshop.colombia.order.domain.model.OrderItem;
import com.eliteshop.colombia.order.domain.model.OrderShippingAddress;
import com.eliteshop.colombia.order.domain.model.OrderShippingCity;
import com.eliteshop.colombia.order.domain.model.OrderShippingDepartment;
import com.eliteshop.colombia.order.domain.model.OrderStatus;
import com.eliteshop.colombia.order.domain.model.OrderTotalAmount;
import com.eliteshop.colombia.order.domain.model.OrderUpdatedAt;
import com.eliteshop.colombia.order.domain.repository.OrderItemRepository;
import com.eliteshop.colombia.order.infrastructure.controller.dto.OrderItemResponse;
import com.eliteshop.colombia.order.infrastructure.controller.dto.OrderRequest;
import com.eliteshop.colombia.order.infrastructure.controller.dto.OrderResponse;
import com.eliteshop.colombia.order.infrastructure.persistence.OrderEntity;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderMapper {

  private final OrderItemRepository orderItemRepository;

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
        entity.getUpdatedAt() != null ? new OrderUpdatedAt(entity.getUpdatedAt()) : null,
        entity.getTrackingNumber(),
        entity.getShippingCarrier(),
        entity.getShippingLabelUrl(),
        entity.getDisputeReason() != null
            ? DisputeReason.valueOf(entity.getDisputeReason())
            : null);
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
    entity.setTrackingNumber(domain.getTrackingNumber());
    entity.setShippingCarrier(domain.getShippingCarrier());
    entity.setShippingLabelUrl(domain.getShippingLabelUrl());
    if (domain.getDisputeReason() != null) {
      entity.setDisputeReason(domain.getDisputeReason().name());
    }
    return entity;
  }

  public Order toDomainFromRequest(OrderRequest request) {
    if (request == null) {
      return null;
    }
    return toDomainFromRequest(request, request.getCustomerId());
  }

  public Order toDomainFromRequest(OrderRequest request, UUID customerId) {
    if (request == null) {
      return null;
    }
    return new Order(
        new OrderId(UUID.randomUUID()),
        new OrderCustomerId(customerId),
        OrderStatus.PENDING_PAYMENT,
        new OrderTotalAmount(request.getTotalAmount()),
        new OrderShippingAddress(request.getShippingAddress()),
        new OrderShippingDepartment(request.getShippingDepartment()),
        new OrderShippingCity(request.getShippingCity()),
        new OrderCreatedAt(Timestamp.from(Instant.now())),
        null,
        null,
        null,
        null,
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
    response.setTrackingNumber(domain.getTrackingNumber());
    response.setShippingCarrier(domain.getShippingCarrier());
    response.setShippingLabelUrl(domain.getShippingLabelUrl());
    if (domain.getDisputeReason() != null) {
      response.setDisputeReason(domain.getDisputeReason().name());
    }
    return response;
  }

  public OrderResponse toResponseWithItems(Order domain) {
    OrderResponse response = toResponse(domain);
    if (response == null) {
      return null;
    }
    List<OrderItem> items = orderItemRepository.findByOrderId(domain.getId().getValue());
    response.setItems(
        items != null
            ? items.stream().map(this::toItemResponse).collect(Collectors.toList())
            : Collections.emptyList());
    return response;
  }

  private OrderItemResponse toItemResponse(OrderItem item) {
    OrderItemResponse response = new OrderItemResponse();
    response.setId(item.getId().getValue());
    response.setProductId(item.getProductId().getValue());
    response.setSellerId(item.getSellerId().getValue());
    response.setQuantity(item.getQuantity().getValue());
    response.setUnitPrice(item.getUnitPrice().getValue());
    response.setSubtotal(item.getSubtotal());
    return response;
  }
}
