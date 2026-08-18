package com.eliteshop.colombia.order.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.eliteshop.colombia.order.domain.event.OrderStatusChangedEvent;
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
  }

  @Test
  void shouldUpdateOrderAndPublishEventWhenStatusChanges() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    Order existingOrder =
        buildOrder(orderId, customerId, OrderStatus.PENDING, new BigDecimal("200000"));
    Order updatedOrder =
        buildOrder(orderId, customerId, OrderStatus.CONFIRMED, new BigDecimal("200000"));

    when(repository.findById(any(OrderId.class)))
        .thenReturn(Optional.of(existingOrder))
        .thenReturn(Optional.of(existingOrder));

    useCase.execute(updatedOrder);

    verify(repository).update(updatedOrder);

    ArgumentCaptor<OrderStatusChangedEvent> eventCaptor =
        ArgumentCaptor.forClass(OrderStatusChangedEvent.class);
    verify(eventPublisher).publishEvent(eventCaptor.capture());

    OrderStatusChangedEvent event = eventCaptor.getValue();
    assertThat(event.orderId()).isEqualTo(orderId);
    assertThat(event.customerId()).isEqualTo(customerId);
    assertThat(event.previousStatus()).isEqualTo("PENDING");
    assertThat(event.newStatus()).isEqualTo("CONFIRMED");
  }

  @Test
  void shouldNotPublishEventWhenStatusUnchanged() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    Order existingOrder =
        buildOrder(orderId, customerId, OrderStatus.PENDING, new BigDecimal("150000"));
    Order sameStatusOrder =
        buildOrder(orderId, customerId, OrderStatus.PENDING, new BigDecimal("150000"));

    when(repository.findById(any(OrderId.class)))
        .thenReturn(Optional.of(existingOrder))
        .thenReturn(Optional.of(existingOrder));

    useCase.execute(sameStatusOrder);

    verify(repository).update(sameStatusOrder);
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

    verify(repository, never()).update(any());
  }

  @Test
  void shouldCallUpdateOnRepository() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    Order existingOrder =
        buildOrder(orderId, customerId, OrderStatus.PENDING, new BigDecimal("100000"));
    Order updatedOrder =
        buildOrder(orderId, customerId, OrderStatus.DELIVERED, new BigDecimal("100000"));

    when(repository.findById(any(OrderId.class)))
        .thenReturn(Optional.of(existingOrder))
        .thenReturn(Optional.of(existingOrder));

    useCase.execute(updatedOrder);

    verify(repository).update(updatedOrder);
  }

  @Test
  void shouldPublishEventForAllStatusTransitions() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    OrderStatus[] transitions = {
      OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.SHIPPED, OrderStatus.DELIVERED
    };

    for (int i = 0; i < transitions.length - 1; i++) {
      reset(repository, eventPublisher);

      Order existingOrder =
          buildOrder(orderId, customerId, transitions[i], new BigDecimal("100000"));
      Order newOrder =
          buildOrder(orderId, customerId, transitions[i + 1], new BigDecimal("100000"));

      when(repository.findById(any(OrderId.class)))
          .thenReturn(Optional.of(existingOrder))
          .thenReturn(Optional.of(existingOrder));

      useCase.execute(newOrder);

      ArgumentCaptor<OrderStatusChangedEvent> captor =
          ArgumentCaptor.forClass(OrderStatusChangedEvent.class);
      verify(eventPublisher).publishEvent(captor.capture());
      assertThat(captor.getValue().previousStatus()).isEqualTo(transitions[i].name());
      assertThat(captor.getValue().newStatus()).isEqualTo(transitions[i + 1].name());
    }
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
        null);
  }
}
