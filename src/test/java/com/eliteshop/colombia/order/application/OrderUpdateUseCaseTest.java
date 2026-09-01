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
class OrderUpdateUseCaseTest {

  @Mock private OrderRepository repository;

  @Mock private ApplicationEventPublisher eventPublisher;

  private OrderUpdateUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new OrderUpdateUseCase(repository, eventPublisher);
    lenient().when(repository.updateStatusIfCurrent(any(), any(), any(), any())).thenReturn(true);
  }

  @Test
  void shouldUpdateOrderAndPublishEventWhenStatusChanges() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    Order existingOrder =
        buildOrder(orderId, customerId, OrderStatus.PENDING_PAYMENT, new BigDecimal("200000"));
    Order updatedOrder =
        buildOrder(orderId, customerId, OrderStatus.PAID, new BigDecimal("200000"));

    when(repository.findById(any(OrderId.class)))
        .thenReturn(Optional.of(existingOrder))
        .thenReturn(Optional.of(existingOrder));

    useCase.execute(updatedOrder);

    verify(repository)
        .updateStatusIfCurrent(any(), eq(OrderStatus.PENDING_PAYMENT), eq(OrderStatus.PAID), any());

    ArgumentCaptor<OrderStatusChangedEvent> eventCaptor =
        ArgumentCaptor.forClass(OrderStatusChangedEvent.class);
    verify(eventPublisher).publishEvent(eventCaptor.capture());

    OrderStatusChangedEvent event = eventCaptor.getValue();
    assertThat(event.orderId()).isEqualTo(orderId);
    assertThat(event.customerId()).isEqualTo(customerId);
    assertThat(event.previousStatus()).isEqualTo("PENDING_PAYMENT");
    assertThat(event.newStatus()).isEqualTo("PAID");
  }

  @Test
  void shouldNotPublishEventWhenStatusUnchanged() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    Order existingOrder =
        buildOrder(orderId, customerId, OrderStatus.PENDING_PAYMENT, new BigDecimal("150000"));
    Order sameStatusOrder =
        buildOrder(orderId, customerId, OrderStatus.PENDING_PAYMENT, new BigDecimal("150000"));

    when(repository.findById(any(OrderId.class)))
        .thenReturn(Optional.of(existingOrder))
        .thenReturn(Optional.of(existingOrder));

    useCase.execute(sameStatusOrder);

    verify(repository, never()).updateStatusIfCurrent(any(), any(), any(), any());
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void shouldThrowWhenOrderNotFound() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    Order order = buildOrder(orderId, customerId, OrderStatus.SHIPPED, new BigDecimal("300000"));

    when(repository.findById(any(OrderId.class))).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.execute(order))
        .isInstanceOf(OrderNotFoundException.class)
        .hasMessage("The order not exist in our platform");

    verify(repository, never()).updateStatusIfCurrent(any(), any(), any(), any());
  }

  @Test
  void shouldCallUpdateStatusIfCurrentOnRepository() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    Order existingOrder =
        buildOrder(orderId, customerId, OrderStatus.PAID, new BigDecimal("100000"));
    Order updatedOrder =
        buildOrder(orderId, customerId, OrderStatus.IN_PREPARATION, new BigDecimal("100000"));

    when(repository.findById(any(OrderId.class)))
        .thenReturn(Optional.of(existingOrder))
        .thenReturn(Optional.of(existingOrder));

    useCase.execute(updatedOrder);

    verify(repository)
        .updateStatusIfCurrent(any(), eq(OrderStatus.PAID), eq(OrderStatus.IN_PREPARATION), any());
  }

  @Test
  void shouldPublishEventForAllValidTransitions() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    OrderStatus[][] transitions = {
      {OrderStatus.PENDING_PAYMENT, OrderStatus.PAID},
      {OrderStatus.PAID, OrderStatus.IN_PREPARATION},
      {OrderStatus.IN_PREPARATION, OrderStatus.SHIPPED},
      {OrderStatus.SHIPPED, OrderStatus.OUT_FOR_DELIVERY},
      {OrderStatus.OUT_FOR_DELIVERY, OrderStatus.DELIVERED},
      {OrderStatus.DELIVERED, OrderStatus.COMPLETED},
    };

    for (OrderStatus[] transition : transitions) {
      reset(repository, eventPublisher);
      lenient().when(repository.updateStatusIfCurrent(any(), any(), any(), any())).thenReturn(true);

      Order existingOrder =
          buildOrder(orderId, customerId, transition[0], new BigDecimal("100000"));
      Order newOrder = buildOrder(orderId, customerId, transition[1], new BigDecimal("100000"));

      when(repository.findById(any(OrderId.class)))
          .thenReturn(Optional.of(existingOrder))
          .thenReturn(Optional.of(existingOrder));

      useCase.execute(newOrder);

      ArgumentCaptor<OrderStatusChangedEvent> captor =
          ArgumentCaptor.forClass(OrderStatusChangedEvent.class);
      verify(eventPublisher).publishEvent(captor.capture());
      assertThat(captor.getValue().previousStatus()).isEqualTo(transition[0].name());
      assertThat(captor.getValue().newStatus()).isEqualTo(transition[1].name());
    }
  }

  @Test
  void shouldThrowWhenTransitionIsInvalid() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    Order existingOrder =
        buildOrder(orderId, customerId, OrderStatus.PENDING_PAYMENT, new BigDecimal("100000"));
    Order invalidOrder =
        buildOrder(orderId, customerId, OrderStatus.SHIPPED, new BigDecimal("100000"));

    when(repository.findById(any(OrderId.class)))
        .thenReturn(Optional.of(existingOrder))
        .thenReturn(Optional.of(existingOrder));

    assertThatThrownBy(() -> useCase.execute(invalidOrder))
        .isInstanceOf(InvalidOrderStatusTransitionException.class)
        .hasMessageContaining("Invalid status transition");

    verify(repository, never()).updateStatusIfCurrent(any(), any(), any(), any());
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
