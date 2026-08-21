package com.eliteshop.colombia.order.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
class OrderFindByIdUseCaseTest {

  @Mock private OrderRepository repository;

  private OrderFindByIdUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new OrderFindByIdUseCase(repository);
  }

  @Test
  void shouldReturnOrderWhenExists() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();
    OrderId id = new OrderId(orderId);

    Order order =
        buildOrder(orderId, customerId, OrderStatus.PENDING_PAYMENT, new BigDecimal("200000"));

    when(repository.findById(id)).thenReturn(Optional.of(order));

    Optional<Order> result = useCase.execute(id);

    assertThat(result).isPresent();
    assertThat(result.get().getId().getValue()).isEqualTo(orderId);
    assertThat(result.get().getCustomerId().getValue()).isEqualTo(customerId);
    assertThat(result.get().getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
    assertThat(result.get().getTotalAmount().getValue())
        .isEqualByComparingTo(new BigDecimal("200000"));
  }

  @Test
  void shouldReturnEmptyWhenOrderNotFound() {
    UUID orderId = UUID.randomUUID();
    OrderId id = new OrderId(orderId);

    when(repository.findById(id)).thenReturn(Optional.empty());

    Optional<Order> result = useCase.execute(id);

    assertThat(result).isEmpty();
  }

  @Test
  void shouldReturnEmptyWhenIdIsNull() {
    Optional<Order> result = useCase.execute(null);

    assertThat(result).isEmpty();
    verify(repository, never()).findById(any());
  }

  @Test
  void shouldCallRepositoryWithCorrectId() {
    UUID orderId = UUID.randomUUID();
    OrderId id = new OrderId(orderId);

    when(repository.findById(id)).thenReturn(Optional.empty());

    useCase.execute(id);

    verify(repository).findById(id);
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
