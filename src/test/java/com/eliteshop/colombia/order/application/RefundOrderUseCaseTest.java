package com.eliteshop.colombia.order.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.eliteshop.colombia.order.domain.event.OrderStatusChangedEvent;
import com.eliteshop.colombia.order.domain.exception.InvalidOrderStatusTransitionException;
import com.eliteshop.colombia.order.domain.exception.OrderNotFoundException;
import com.eliteshop.colombia.order.domain.model.*;
import com.eliteshop.colombia.order.domain.repository.OrderItemRepository;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import com.eliteshop.colombia.product.domain.repository.ProductRepository;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
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
  @Mock private OrderItemRepository orderItemRepository;
  @Mock private ProductRepository productRepository;
  @Mock private ApplicationEventPublisher eventPublisher;

  private RefundOrderUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase =
        new RefundOrderUseCase(repository, orderItemRepository, productRepository, eventPublisher);
    lenient().when(repository.updateStatusIfCurrent(any(), any(), any(), any())).thenReturn(true);
    lenient()
        .when(orderItemRepository.findByOrderId(any(UUID.class)))
        .thenReturn(Collections.emptyList());
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

    verify(repository, never()).updateStatusIfCurrent(any(), any(), any(), any());
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

  @Test
  void shouldRestoreStockWhenDisputeReasonIsRestockable() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();
    UUID productId = UUID.randomUUID();

    Order order =
        buildOrderWithReason(
            orderId,
            customerId,
            OrderStatus.DISPUTE,
            new BigDecimal("200000"),
            DisputeReason.WRONG_ITEM);

    OrderItem item =
        OrderItem.create(orderId, productId, UUID.randomUUID(), 2, new BigDecimal("100000"));

    when(repository.findById(any(OrderId.class))).thenReturn(Optional.of(order));
    when(orderItemRepository.findByOrderId(orderId)).thenReturn(List.of(item));

    useCase.execute(new OrderId(orderId), customerId);

    verify(productRepository).restoreStock(any(), eq(2));
  }

  @Test
  void shouldNotRestoreStockWhenDisputeReasonIsNotRestockable() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    Order order =
        buildOrderWithReason(
            orderId,
            customerId,
            OrderStatus.DISPUTE,
            new BigDecimal("200000"),
            DisputeReason.PRODUCT_DAMAGED);

    when(repository.findById(any(OrderId.class))).thenReturn(Optional.of(order));

    useCase.execute(new OrderId(orderId), customerId);

    verify(orderItemRepository, never()).findByOrderId(any());
    verify(productRepository, never()).restoreStock(any(), anyInt());
  }

  @Test
  void shouldRestoreStockWhenDisputeReasonIsNull() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    Order order = buildOrder(orderId, customerId, OrderStatus.DISPUTE, new BigDecimal("200000"));

    when(repository.findById(any(OrderId.class))).thenReturn(Optional.of(order));
    when(orderItemRepository.findByOrderId(orderId)).thenReturn(Collections.emptyList());

    useCase.execute(new OrderId(orderId), customerId);

    verify(orderItemRepository).findByOrderId(orderId);
    verify(productRepository, never()).restoreStock(any(), anyInt());
  }

  private Order buildOrderWithReason(
      UUID orderId,
      UUID customerId,
      OrderStatus status,
      BigDecimal totalAmount,
      DisputeReason disputeReason) {
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
        disputeReason);
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
