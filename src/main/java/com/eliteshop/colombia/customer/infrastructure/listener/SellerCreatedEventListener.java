package com.eliteshop.colombia.customer.infrastructure.listener;

import com.eliteshop.colombia.seller.domain.event.SellerCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SellerCreatedEventListener {

  @Async
  @EventListener
  public void handleSellerCreated(SellerCreatedEvent event) {
    log.info(
        "SellerCreatedEvent recibido: sellerId={}, email={}. El customer se mantiene activo para poder comprar.",
        event.sellerId(),
        event.email());

    log.info("SellerCreatedEvent procesado para sellerId={}", event.sellerId());
  }
}
