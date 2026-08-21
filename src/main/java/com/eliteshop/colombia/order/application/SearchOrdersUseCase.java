package com.eliteshop.colombia.order.application;

import com.eliteshop.colombia.order.domain.model.Order;
import com.eliteshop.colombia.order.domain.model.OrderId;
import com.eliteshop.colombia.order.domain.model.OrderStatus;
import com.eliteshop.colombia.order.domain.repository.OrderItemRepository;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SearchOrdersUseCase {

  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;

  public record SearchResult(List<Order> orders, int total, int page, int size, int totalPages) {}

  public SearchResult execute(UUID sellerId, OrderStatus status, int page, int size) {
    log.info(
        "Buscando ordenes para sellerId: {}, status: {}, page: {}, size: {}",
        sellerId,
        status,
        page,
        size);

    List<UUID> orderIds = orderItemRepository.findDistinctOrderIdsBySellerId(sellerId);

    List<Order> allOrders =
        orderIds.stream()
            .map(orderId -> orderRepository.findById(new OrderId(orderId)))
            .filter(java.util.Optional::isPresent)
            .map(java.util.Optional::get)
            .collect(Collectors.toList());

    if (status != null) {
      allOrders =
          allOrders.stream().filter(o -> o.getStatus() == status).collect(Collectors.toList());
    }

    int total = allOrders.size();
    int totalPages = (int) Math.ceil((double) total / size);

    List<Order> pagedOrders =
        allOrders.stream().skip((long) page * size).limit(size).collect(Collectors.toList());

    log.info(
        "Encontradas {} ordenes (página {}/{} de {})",
        pagedOrders.size(),
        page + 1,
        totalPages,
        total);

    return new SearchResult(pagedOrders, total, page, size, totalPages);
  }
}
