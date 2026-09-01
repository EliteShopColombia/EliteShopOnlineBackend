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
class ConfirmDeliveryUseCaseTest {

  @Mock private OrderRepository repository;
  @Mock private ApplicationEventPublisher eventPublisher;

  private ConfirmDeliveryUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new ConfirmDeliveryUseCase(repository, eventPublisher);
    lenient().when(repository.updateStatusIfCurrent(any(), any(), any(), any())).thenReturn(true);
  }

  @Test
  void shouldConfirmDeliveryForOutForDeliveryOrder() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    Order order =
        buildOrder(orderId, customerId, OrderStatus.OUT_FOR_DELIVERY, new BigDecimal("200000"));

    when(repository.findById(any(OrderId.class))).thenReturn(Optional.of(order));

    useCase.execute(new OrderId(orderId));

    verify(repository)
        .updateStatusIfCurrent(
            any(), eq(OrderStatus.OUT_FOR_DELIVERY), eq(OrderStatus.DELIVERED), any());

    ArgumentCaptor<OrderStatusChangedEvent> eventCaptor =
        ArgumentCaptor.forClass(OrderStatusChangedEvent.class);
    verify(eventPublisher).publishEvent(eventCaptor.capture());
    assertThat(eventCaptor.getValue().previousStatus()).isEqualTo("OUT_FOR_DELIVERY");
    assertThat(eventCaptor.getValue().newStatus()).isEqualTo("DELIVERED");
  }

  @Test
  void shouldThrowWhenOrderNotFound() {
    UUID orderId = UUID.randomUUID();

    when(repository.findById(any(OrderId.class))).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.execute(new OrderId(orderId)))
        .isInstanceOf(OrderNotFoundException.class)
        .hasMessage("The order not exist in our platform");

    verify(repository, never()).updateStatusIfCurrent(any(), any(), any(), any());
  }

  @Test
  void shouldThrowWhenStatusIsNotOutForDelivery() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    Order order = buildOrder(orderId, customerId, OrderStatus.SHIPPED, new BigDecimal("200000"));

    when(repository.findById(any(OrderId.class))).thenReturn(Optional.of(order));

    assertThatThrownBy(() -> useCase.execute(new OrderId(orderId)))
        .isInstanceOf(InvalidOrderStatusTransitionException.class)
        .hasMessageContaining("Only OUT_FOR_DELIVERY orders can be confirmed as delivered");

    verify(repository, never()).updateStatusIfCurrent(any(), any(), any(), any());
  }

  @Test
  void shouldThrowWhenStatusIsPaid() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    Order order = buildOrder(orderId, customerId, OrderStatus.PAID, new BigDecimal("100000"));

    when(repository.findById(any(OrderId.class))).thenReturn(Optional.of(order));

    assertThatThrownBy(() -> useCase.execute(new OrderId(orderId)))
        .isInstanceOf(InvalidOrderStatusTransitionException.class)
        .hasMessageContaining("Only OUT_FOR_DELIVERY orders can be confirmed as delivered");

    verify(repository, never()).updateStatusIfCurrent(any(), any(), any(), any());
  }

  @Test
  void shouldThrowWhenStatusIsAlreadyDelivered() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    Order order = buildOrder(orderId, customerId, OrderStatus.DELIVERED, new BigDecimal("100000"));

    when(repository.findById(any(OrderId.class))).thenReturn(Optional.of(order));

    assertThatThrownBy(() -> useCase.execute(new OrderId(orderId)))
        .isInstanceOf(InvalidOrderStatusTransitionException.class)
        .hasMessageContaining("Only OUT_FOR_DELIVERY orders can be confirmed as delivered");
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
