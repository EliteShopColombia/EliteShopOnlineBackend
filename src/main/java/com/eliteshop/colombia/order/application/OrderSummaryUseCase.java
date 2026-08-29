package com.eliteshop.colombia.order.application;

import com.eliteshop.colombia.order.domain.model.Order;
import com.eliteshop.colombia.order.domain.repository.OrderItemRepository;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import com.eliteshop.colombia.order.infrastructure.controller.dto.OrderSummaryResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class OrderSummaryUseCase {

  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;

  public OrderSummaryResponse execute(UUID sellerId) {
    log.info("Calculando resumen de ordenes para sellerId: {}", sellerId);

    List<UUID> orderIds = orderItemRepository.findDistinctOrderIdsBySellerId(sellerId);

    List<Order> orders = orderRepository.findAllByIds(orderIds);

    int totalOrders = orders.size();
    BigDecimal totalRevenue =
        orders.stream()
            .map(o -> o.getTotalAmount().getValue())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal averageOrderValue =
        totalOrders > 0
            ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

    Map<String, Integer> ordersByStatus = new LinkedHashMap<>();
    for (Order order : orders) {
      String status = order.getStatus().name();
      ordersByStatus.merge(status, 1, Integer::sum);
    }

    log.info(
        "Resumen para sellerId: {} - {} ordenes, ingreso total: {}",
        sellerId,
        totalOrders,
        totalRevenue);

    return new OrderSummaryResponse(
        sellerId, totalOrders, totalRevenue, ordersByStatus, averageOrderValue);
  }
}
