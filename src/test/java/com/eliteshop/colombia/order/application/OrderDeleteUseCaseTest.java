package com.eliteshop.colombia.order.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderDeleteUseCaseTest {

  @Mock private OrderRepository repository;

  private OrderDeleteUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new OrderDeleteUseCase(repository);
  }

  @Test
  void shouldDeleteOrderWhenExists() {
    UUID orderId = UUID.randomUUID();
    OrderId id = new OrderId(orderId);

    Order existingOrder =
        buildOrder(orderId, UUID.randomUUID(), OrderStatus.PENDING, new BigDecimal("100000"));

    when(repository.findById(id)).thenReturn(Optional.of(existingOrder));

    useCase.execute(id);

    verify(repository).delete(id);
  }

  @Test
  void shouldThrowWhenOrderNotFound() {
    UUID orderId = UUID.randomUUID();
    OrderId id = new OrderId(orderId);

    when(repository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.execute(id))
        .isInstanceOf(OrderNotFoundException.class)
        .hasMessage("The order not exist in our platform");

    verify(repository, never()).delete(id);
  }

  @Test
  void shouldDeleteOrderWhenCancelled() {
    UUID orderId = UUID.randomUUID();
    OrderId id = new OrderId(orderId);

    Order existingOrder =
        buildOrder(orderId, UUID.randomUUID(), OrderStatus.CANCELLED, new BigDecimal("50000"));

    when(repository.findById(id)).thenReturn(Optional.of(existingOrder));

    useCase.execute(id);

    verify(repository).delete(id);
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
