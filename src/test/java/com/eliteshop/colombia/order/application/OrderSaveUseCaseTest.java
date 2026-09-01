package com.eliteshop.colombia.order.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.eliteshop.colombia.order.domain.event.OrderCreatedEvent;
import com.eliteshop.colombia.order.domain.exception.OrderAlreadyExistsException;
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
class OrderSaveUseCaseTest {

  @Mock private OrderRepository repository;

  @Mock private ApplicationEventPublisher eventPublisher;

  private OrderSaveUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new OrderSaveUseCase(repository, eventPublisher);
  }

  @Test
  void shouldSaveOrderAndPublishEvent() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    Order order =
        buildOrder(orderId, customerId, OrderStatus.PENDING_PAYMENT, new BigDecimal("250000"));

    when(repository.findById(any(OrderId.class))).thenReturn(Optional.empty());

    useCase.execute(order);

    verify(repository).save(order);

    ArgumentCaptor<OrderCreatedEvent> eventCaptor =
        ArgumentCaptor.forClass(OrderCreatedEvent.class);
    verify(eventPublisher).publishEvent(eventCaptor.capture());

    OrderCreatedEvent event = eventCaptor.getValue();
    assertThat(event.orderId()).isEqualTo(orderId);
    assertThat(event.customerId()).isEqualTo(customerId);
    assertThat(event.totalAmount()).isEqualByComparingTo(new BigDecimal("250000"));
    assertThat(event.occurredAt()).isNotNull();
  }

  @Test
  void shouldThrowWhenOrderAlreadyExists() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    Order order =
        buildOrder(orderId, customerId, OrderStatus.PENDING_PAYMENT, new BigDecimal("150000"));
    Order existingOrder =
        buildOrder(orderId, customerId, OrderStatus.PAID, new BigDecimal("150000"));

    when(repository.findById(any(OrderId.class))).thenReturn(Optional.of(existingOrder));

    assertThatThrownBy(() -> useCase.execute(order))
        .isInstanceOf(OrderAlreadyExistsException.class)
        .hasMessage("This order already exist in the platform");

    verify(repository, never()).save(any());
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void shouldPublishEventWithCorrectAmount() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();
    BigDecimal amount = new BigDecimal("999999.99");

    Order order = buildOrder(orderId, customerId, OrderStatus.PENDING_PAYMENT, amount);

    when(repository.findById(any(OrderId.class))).thenReturn(Optional.empty());

    useCase.execute(order);

    ArgumentCaptor<OrderCreatedEvent> eventCaptor =
        ArgumentCaptor.forClass(OrderCreatedEvent.class);
    verify(eventPublisher).publishEvent(eventCaptor.capture());

    assertThat(eventCaptor.getValue().totalAmount()).isEqualByComparingTo(amount);
  }

  @Test
  void shouldSaveOrderWithAllFields() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    Order order =
        buildOrder(orderId, customerId, OrderStatus.PENDING_PAYMENT, new BigDecimal("100000"));

    when(repository.findById(any(OrderId.class))).thenReturn(Optional.empty());

    useCase.execute(order);

    ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
    verify(repository).save(captor.capture());

    Order saved = captor.getValue();
    assertThat(saved.getId().getValue()).isEqualTo(orderId);
    assertThat(saved.getCustomerId().getValue()).isEqualTo(customerId);
    assertThat(saved.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
    assertThat(saved.getShippingAddress().getValue()).isEqualTo("Calle 100 #15-20");
    assertThat(saved.getShippingDepartment().getValue()).isEqualTo("Bogota");
    assertThat(saved.getShippingCity().getValue()).isEqualTo("Bogota D.C.");
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
