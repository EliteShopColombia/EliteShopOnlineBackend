package com.eliteshop.colombia.order.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

@ExtendWith(MockitoExtension.class)
class UpdateTrackingUseCaseTest {

  @Mock private OrderRepository repository;

  private UpdateTrackingUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new UpdateTrackingUseCase(repository);
  }

  @Test
  void shouldUpdateTrackingInfo() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    Order order = buildOrder(orderId, customerId, OrderStatus.SHIPPED, new BigDecimal("200000"));

    when(repository.findById(any(OrderId.class))).thenReturn(Optional.of(order));

    useCase.execute(new OrderId(orderId), "TRK-12345", "Servientrega", "https://label.com/123");

    ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
    verify(repository).update(orderCaptor.capture());

    Order updatedOrder = orderCaptor.getValue();
    assertThat(updatedOrder.getTrackingNumber()).isEqualTo("TRK-12345");
    assertThat(updatedOrder.getShippingCarrier()).isEqualTo("Servientrega");
    assertThat(updatedOrder.getShippingLabelUrl()).isEqualTo("https://label.com/123");
    assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.SHIPPED);
  }

  @Test
  void shouldThrowWhenOrderNotFound() {
    UUID orderId = UUID.randomUUID();
    when(repository.findById(any(OrderId.class))).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.execute(new OrderId(orderId), "TRK-123", "TCC", null))
        .isInstanceOf(OrderNotFoundException.class);

    verify(repository, never()).update(any());
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
