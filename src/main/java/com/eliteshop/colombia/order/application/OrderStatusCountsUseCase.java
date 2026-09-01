package com.eliteshop.colombia.order.application;

import com.eliteshop.colombia.order.domain.model.OrderStatus;
import com.eliteshop.colombia.order.domain.repository.OrderItemRepository;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import com.eliteshop.colombia.order.infrastructure.controller.dto.OrderStatusCountResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class OrderStatusCountsUseCase {

  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;

  public OrderStatusCountResponse execute(UUID sellerId) {
    log.info("Contando ordenes por estado para sellerId: {}", sellerId);

    List<UUID> orderIds = orderItemRepository.findDistinctOrderIdsBySellerId(sellerId);

    Map<String, Integer> counts = new LinkedHashMap<>();
    for (OrderStatus status : OrderStatus.values()) {
      counts.put(status.name(), 0);
    }

    List<com.eliteshop.colombia.order.domain.model.Order> orders =
        orderRepository.findAllByIds(orderIds);
    for (com.eliteshop.colombia.order.domain.model.Order order : orders) {
      counts.merge(order.getStatus().name(), 1, Integer::sum);
    }

    int total = orderIds.size();

    log.info("SellerId {} tiene {} ordenes totales", sellerId, total);

    return new OrderStatusCountResponse(sellerId, total, counts);
  }
}
