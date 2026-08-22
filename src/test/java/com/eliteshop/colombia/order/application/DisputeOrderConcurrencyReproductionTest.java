package com.eliteshop.colombia.order.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.eliteshop.colombia.order.domain.event.OrderStatusChangedEvent;
import com.eliteshop.colombia.order.domain.model.Order;
import com.eliteshop.colombia.order.domain.model.OrderCreatedAt;
import com.eliteshop.colombia.order.domain.model.OrderCustomerId;
import com.eliteshop.colombia.order.domain.model.OrderId;
import com.eliteshop.colombia.order.domain.model.OrderShippingAddress;
import com.eliteshop.colombia.order.domain.model.OrderShippingCity;
import com.eliteshop.colombia.order.domain.model.OrderShippingDepartment;
import com.eliteshop.colombia.order.domain.model.OrderStatus;
import com.eliteshop.colombia.order.domain.model.OrderTotalAmount;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

class DisputeOrderConcurrencyReproductionTest {

  @Test
  void concurrentRequestsAllowOnlyOneDisputeAndEvent() throws Exception {
    OrderRepository repository = mock(OrderRepository.class);
    ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    UUID orderId = UUID.randomUUID();
    Order order = buildOrder(orderId);
    CountDownLatch readsStarted = new CountDownLatch(2);
    CountDownLatch releaseReads = new CountDownLatch(1);

    when(repository.findById(any(OrderId.class)))
        .thenAnswer(
            invocation -> {
              readsStarted.countDown();
              assertThat(readsStarted.await(5, TimeUnit.SECONDS)).isTrue();
              releaseReads.countDown();
              return Optional.of(order);
            });
    when(repository.updateStatusIfCurrent(any(), any(), any(), any())).thenReturn(true, false);

    DisputeOrderUseCase useCase = new DisputeOrderUseCase(repository, eventPublisher);
    ExecutorService executor = Executors.newFixedThreadPool(2);
    try {
      UUID customerId = order.getCustomerId().getValue();
      var first = executor.submit(() -> useCase.execute(new OrderId(orderId), customerId));
      var second = executor.submit(() -> useCase.execute(new OrderId(orderId), customerId));

      int successfulRequests = 0;
      int rejectedRequests = 0;
      for (var result : java.util.List.of(first, second)) {
        try {
          result.get(5, TimeUnit.SECONDS);
          successfulRequests++;
        } catch (java.util.concurrent.ExecutionException exception) {
          assertThat(exception.getCause())
              .isInstanceOf(
                  com.eliteshop.colombia.order.domain.exception
                      .InvalidOrderStatusTransitionException.class);
          rejectedRequests++;
        }
      }
      assertThat(successfulRequests).isEqualTo(1);
      assertThat(rejectedRequests).isEqualTo(1);
    } finally {
      releaseReads.countDown();
      executor.shutdownNow();
    }

    verify(repository, times(2)).updateStatusIfCurrent(any(), any(), any(), any());
    verify(eventPublisher, times(1)).publishEvent(any(OrderStatusChangedEvent.class));
  }

  private Order buildOrder(UUID orderId) {
    return new Order(
        new OrderId(orderId),
        new OrderCustomerId(UUID.randomUUID()),
        OrderStatus.COMPLETED,
        new OrderTotalAmount(new BigDecimal("200000")),
        new OrderShippingAddress("Calle 100 #15-20"),
        new OrderShippingDepartment("Bogota"),
        new OrderShippingCity("Bogota D.C."),
        new OrderCreatedAt(Timestamp.from(Instant.now())),
        null,
        null,
        null,
        null);
  }
}
