package com.eliteshop.colombia.order.application;

import com.eliteshop.colombia.order.domain.model.Order;
import com.eliteshop.colombia.order.domain.model.OrderCustomerId;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import com.eliteshop.colombia.shared.domain.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class FindOrdersByCustomerIdUseCase {

  private final OrderRepository repository;

  public PageResult<Order> execute(OrderCustomerId customerId, int page, int size) {
    log.info(
        "Buscando ordenes por customerId: {} (page={}, size={})",
        customerId.getValue(),
        page,
        size);
    PageResult<Order> result = repository.findPageByCustomerId(customerId.getValue(), page, size);
    log.info(
        "Se encontraron {} ordenes para customerId: {} (total: {})",
        result.content().size(),
        customerId.getValue(),
        result.totalElements());
    return result;
  }
}
