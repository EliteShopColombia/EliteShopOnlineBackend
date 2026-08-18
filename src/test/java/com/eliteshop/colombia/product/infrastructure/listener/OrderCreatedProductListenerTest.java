package com.eliteshop.colombia.product.infrastructure.listener;

import static org.mockito.Mockito.*;

import com.eliteshop.colombia.order.domain.event.OrderCreatedEvent;
import com.eliteshop.colombia.product.infrastructure.persistence.ProductJpaRepository;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderCreatedProductListenerTest {

  @Mock private ProductJpaRepository productRepository;

  private OrderCreatedProductListener listener;

  @BeforeEach
  void setUp() {
    listener = new OrderCreatedProductListener(productRepository);
  }

  @Test
  void shouldHandleOrderCreatedEvent() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    OrderCreatedEvent event =
        new OrderCreatedEvent(
            orderId, customerId, new BigDecimal("250000"), java.time.Instant.now());

    listener.handleOrderCreated(event);

    verify(productRepository, never()).save(any());
  }

  @Test
  void shouldNotThrowOnEventHandling() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    OrderCreatedEvent event =
        new OrderCreatedEvent(
            orderId, customerId, new BigDecimal("150000"), java.time.Instant.now());

    listener.handleOrderCreated(event);
  }
}
