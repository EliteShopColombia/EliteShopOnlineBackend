package com.eliteshop.colombia.product.infrastructure.listener;

import com.eliteshop.colombia.order.domain.event.OrderCreatedEvent;
import com.eliteshop.colombia.product.infrastructure.persistence.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedProductListener {

  private final ProductJpaRepository productRepository;

  @Async
  @EventListener
  @Transactional
  public void handleOrderCreated(OrderCreatedEvent event) {
    log.info(
        "OrderCreated received - orderId={}, customerId={}, totalAmount={}",
        event.orderId(),
        event.customerId(),
        event.totalAmount());

    log.info(
        "Stock reservation queued for order {} - awaiting order items implementation",
        event.orderId());
  }
}
