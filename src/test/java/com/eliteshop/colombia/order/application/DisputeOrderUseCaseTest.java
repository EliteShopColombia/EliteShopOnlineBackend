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
class DisputeOrderUseCaseTest {

  @Mock private OrderRepository repository;
  @Mock private ApplicationEventPublisher eventPublisher;

  private DisputeOrderUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new DisputeOrderUseCase(repository, eventPublisher);
    lenient().when(repository.updateStatusIfCurrent(any(), any(), any(), any())).thenReturn(true);
    lenient().doNothing().when(repository).updateDisputeReason(any(), any());
  }

  @Test
  void shouldOpenDisputeForInPreparationOrder() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    Order order =
        buildOrder(orderId, customerId, OrderStatus.IN_PREPARATION, new BigDecimal("200000"));

    when(repository.findById(any(OrderId.class))).thenReturn(Optional.of(order));

    useCase.execute(new OrderId(orderId), customerId, DisputeReason.NO_LONGER_NEEDED);

    verify(repository)
        .updateStatusIfCurrent(
            any(), eq(OrderStatus.IN_PREPARATION), eq(OrderStatus.DISPUTE), any());

    ArgumentCaptor<OrderStatusChangedEvent> eventCaptor =
        ArgumentCaptor.forClass(OrderStatusChangedEvent.class);
    verify(eventPublisher).publishEvent(eventCaptor.capture());
    assertThat(eventCaptor.getValue().previousStatus()).isEqualTo("IN_PREPARATION");
    assertThat(eventCaptor.getValue().newStatus()).isEqualTo("DISPUTE");
  }

  @Test
  void shouldOpenDisputeForShippedOrder() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    Order order = buildOrder(orderId, customerId, OrderStatus.SHIPPED, new BigDecimal("200000"));

    when(repository.findById(any(OrderId.class))).thenReturn(Optional.of(order));

    useCase.execute(new OrderId(orderId), customerId, DisputeReason.NO_LONGER_NEEDED);

    verify(repository)
        .updateStatusIfCurrent(any(), eq(OrderStatus.SHIPPED), eq(OrderStatus.DISPUTE), any());
  }

  @Test
  void shouldOpenDisputeForDeliveredOrder() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    Order order = buildOrder(orderId, customerId, OrderStatus.DELIVERED, new BigDecimal("200000"));

    when(repository.findById(any(OrderId.class))).thenReturn(Optional.of(order));

    useCase.execute(new OrderId(orderId), customerId, DisputeReason.NO_LONGER_NEEDED);

    verify(repository)
        .updateStatusIfCurrent(any(), eq(OrderStatus.DELIVERED), eq(OrderStatus.DISPUTE), any());
  }

  @Test
  void shouldOpenDisputeForCompletedOrder() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    Order order = buildOrder(orderId, customerId, OrderStatus.COMPLETED, new BigDecimal("200000"));

    when(repository.findById(any(OrderId.class))).thenReturn(Optional.of(order));

    useCase.execute(new OrderId(orderId), customerId, DisputeReason.NO_LONGER_NEEDED);

    verify(repository)
        .updateStatusIfCurrent(any(), eq(OrderStatus.COMPLETED), eq(OrderStatus.DISPUTE), any());
  }

  @Test
  void shouldThrowWhenOrderNotFound() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();
    when(repository.findById(any(OrderId.class))).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> useCase.execute(new OrderId(orderId), customerId, DisputeReason.NO_LONGER_NEEDED))
        .isInstanceOf(OrderNotFoundException.class);

    verify(repository, never()).updateStatusIfCurrent(any(), any(), any(), any());
  }

  @Test
  void shouldThrowWhenStatusIsPendingPayment() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    Order order =
        buildOrder(orderId, customerId, OrderStatus.PENDING_PAYMENT, new BigDecimal("100000"));

    when(repository.findById(any(OrderId.class))).thenReturn(Optional.of(order));

    assertThatThrownBy(
            () -> useCase.execute(new OrderId(orderId), customerId, DisputeReason.NO_LONGER_NEEDED))
        .isInstanceOf(InvalidOrderStatusTransitionException.class)
        .hasMessageContaining("Cannot open dispute for order with status");

    verify(repository, never()).updateStatusIfCurrent(any(), any(), any(), any());
  }

  @Test
  void shouldThrowWhenStatusIsCancelled() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    Order order = buildOrder(orderId, customerId, OrderStatus.CANCELLED, new BigDecimal("100000"));

    when(repository.findById(any(OrderId.class))).thenReturn(Optional.of(order));

    assertThatThrownBy(
            () -> useCase.execute(new OrderId(orderId), customerId, DisputeReason.NO_LONGER_NEEDED))
        .isInstanceOf(InvalidOrderStatusTransitionException.class)
        .hasMessageContaining("Cannot open dispute for order with status");
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
        null,
        null);
  }
}
