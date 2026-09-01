package com.eliteshop.colombia.order.infrastructure.adapter;

import com.eliteshop.colombia.order.domain.model.OrderItem;
import com.eliteshop.colombia.order.domain.model.OrderItemId;
import com.eliteshop.colombia.order.domain.model.OrderItemOrderId;
import com.eliteshop.colombia.order.domain.model.OrderItemProductId;
import com.eliteshop.colombia.order.domain.model.OrderItemQuantity;
import com.eliteshop.colombia.order.domain.model.OrderItemSellerId;
import com.eliteshop.colombia.order.domain.model.OrderItemUnitPrice;
import com.eliteshop.colombia.order.domain.repository.OrderItemRepository;
import com.eliteshop.colombia.order.infrastructure.persistence.OrderItemEntity;
import com.eliteshop.colombia.order.infrastructure.persistence.OrderItemJpaRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderItemPostgresAdapter implements OrderItemRepository {

  private final OrderItemJpaRepository jpaRepository;

  @Override
  public OrderItem save(OrderItem orderItem) {
    OrderItemEntity entity = jpaRepository.save(toEntity(orderItem));
    return toDomain(entity);
  }

  @Override
  public List<OrderItem> saveAll(List<OrderItem> orderItems) {
    List<OrderItemEntity> entities =
        orderItems.stream().map(this::toEntity).collect(Collectors.toList());
    return jpaRepository.saveAll(entities).stream()
        .map(this::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<OrderItem> findByOrderId(UUID orderId) {
    return jpaRepository.findByOrderId(orderId).stream()
        .map(this::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public void deleteByOrderId(UUID orderId) {
    jpaRepository.deleteByOrderId(orderId);
  }

  @Override
  public boolean existsVerifiedPurchase(UUID customerId, UUID productId) {
    return jpaRepository.existsVerifiedPurchase(customerId, productId);
  }

  @Override
  public List<UUID> findDistinctOrderIdsBySellerId(UUID sellerId) {
    return jpaRepository.findDistinctOrderIdsBySellerId(sellerId);
  }

  private OrderItem toDomain(OrderItemEntity entity) {
    return new OrderItem(
        new OrderItemId(entity.getId()),
        new OrderItemOrderId(entity.getOrderId()),
        new OrderItemProductId(entity.getProductId()),
        new OrderItemSellerId(entity.getSellerId()),
        new OrderItemQuantity(entity.getQuantity()),
        new OrderItemUnitPrice(entity.getUnitPrice()),
        entity.getCreatedAt());
  }

  private OrderItemEntity toEntity(OrderItem domain) {
    OrderItemEntity entity = new OrderItemEntity();
    entity.setId(domain.getId().getValue());
    entity.setOrderId(domain.getOrderId().getValue());
    entity.setProductId(domain.getProductId().getValue());
    entity.setSellerId(domain.getSellerId().getValue());
    entity.setQuantity(domain.getQuantity().getValue());
    entity.setUnitPrice(domain.getUnitPrice().getValue());
    entity.setSubtotal(domain.getSubtotal());
    entity.setCreatedAt(domain.getCreatedAt());
    return entity;
  }
}
