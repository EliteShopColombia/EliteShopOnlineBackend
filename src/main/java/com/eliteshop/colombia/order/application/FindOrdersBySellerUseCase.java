package com.eliteshop.colombia.order.application;

import com.eliteshop.colombia.order.domain.model.Order;
import com.eliteshop.colombia.order.domain.model.OrderId;
import com.eliteshop.colombia.order.domain.repository.OrderItemRepository;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class FindOrdersBySellerUseCase {

  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;

  public List<Order> execute(UUID sellerId) {
    log.info("Buscando ordenes para sellerId: {}", sellerId);

    List<UUID> orderIds = orderItemRepository.findDistinctOrderIdsBySellerId(sellerId);

    List<Order> orders =
        orderIds.stream()
            .map(orderId -> orderRepository.findById(new OrderId(orderId)))
            .filter(java.util.Optional::isPresent)
            .map(java.util.Optional::get)
            .collect(Collectors.toList());

    log.info("Se encontraron {} ordenes para sellerId: {}", orders.size(), sellerId);
    return orders;
  }
}
