package com.eliteshop.colombia.order.application;

import com.eliteshop.colombia.order.domain.model.Order;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import com.eliteshop.colombia.shared.domain.PageResult;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class FindOrdersBySellerUseCase {

  private final OrderRepository orderRepository;

  public PageResult<Order> execute(UUID sellerId, int page, int size) {
    log.info("Buscando ordenes para sellerId: {} (page={}, size={})", sellerId, page, size);
    PageResult<Order> result = orderRepository.findPageBySellerId(sellerId, page, size);
    log.info(
        "Se encontraron {} ordenes para sellerId: {} (total: {})",
        result.content().size(),
        sellerId,
        result.totalElements());
    return result;
  }
}
