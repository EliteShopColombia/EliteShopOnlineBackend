package com.eliteshop.colombia.order.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.eliteshop.colombia.order.domain.event.OrderStatusChangedEvent;
import com.eliteshop.colombia.order.domain.exception.InvalidOrderStatusTransitionException;
import com.eliteshop.colombia.order.domain.exception.OrderNotFoundException;
import com.eliteshop.colombia.order.domain.model.*;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class RefundOrderUseCaseTest {

  @Mock private OrderRepository repository;
  @Mock private ApplicationEventPublisher eventPublisher;

  private RefundOrderUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new RefundOrderUseCase(repository, eventPublisher);
    lenient().when(repository.updateStatusIfCurrent(any(), any(), any(), any())).thenReturn(true);
  }

  @Test
  void shouldRefundDisputedOrder() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    Order order = buildOrder(orderId, customerId, OrderStatus.DISPUTE, new BigDecimal("200000"));

    when(repository.findById(any(OrderId.class))).thenReturn(Optional.of(order));

    useCase.execute(new OrderId(orderId), customerId);

    verify(repository)
        .updateStatusIfCurrent(any(), eq(OrderStatus.DISPUTE), eq(OrderStatus.REFUNDED), any());

    ArgumentCaptor<OrderStatusChangedEvent> eventCaptor =
        ArgumentCaptor.forClass(OrderStatusChangedEvent.class);
    verify(eventPublisher).publishEvent(eventCaptor.capture());
    assertThat(eventCaptor.getValue().previousStatus()).isEqualTo("DISPUTE");
    assertThat(eventCaptor.getValue().newStatus()).isEqualTo("REFUNDED");
  }

  @Test
  void shouldThrowWhenOrderNotFound() {
    UUID orderId = UUID.randomUUID();
    when(repository.findById(any(OrderId.class))).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.execute(new OrderId(orderId), UUID.randomUUID()))
        .isInstanceOf(OrderNotFoundException.class);

    verify(repository, never()).updateStatusIfCurrent(any(), any(), any(), any());
  }

  @Test
  void shouldThrowWhenStatusIsNotDispute() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    Order order = buildOrder(orderId, customerId, OrderStatus.SHIPPED, new BigDecimal("100000"));

    when(repository.findById(any(OrderId.class))).thenReturn(Optional.of(order));

    assertThatThrownBy(() -> useCase.execute(new OrderId(orderId), customerId))
        .isInstanceOf(InvalidOrderStatusTransitionException.class)
        .hasMessageContaining("Only DISPUTE orders can be refunded");

    verify(repository, never()).update(any());
  }

  @Test
  void shouldThrowWhenStatusIsAlreadyRefunded() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    Order order = buildOrder(orderId, customerId, OrderStatus.REFUNDED, new BigDecimal("100000"));

    when(repository.findById(any(OrderId.class))).thenReturn(Optional.of(order));

    assertThatThrownBy(() -> useCase.execute(new OrderId(orderId), customerId))
        .isInstanceOf(InvalidOrderStatusTransitionException.class)
        .hasMessageContaining("Only DISPUTE orders can be refunded");
  }

  private Order buildOrder(
      UUID orderId, UUID customerId, OrderStatus status, BigDecimal totalAmount) {
    return new Order(
        new OrderId(orderId),
        new OrderCustomerId(customerId),
        status,
        new OrderTotalAmount(totalAmount),
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
