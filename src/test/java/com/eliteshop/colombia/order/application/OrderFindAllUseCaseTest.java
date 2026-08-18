package com.eliteshop.colombia.order.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.eliteshop.colombia.order.domain.model.*;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderFindAllUseCaseTest {

  @Mock private OrderRepository repository;

  private OrderFindAllUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new OrderFindAllUseCase(repository);
  }

  @Test
  void shouldReturnAllOrders() {
    UUID orderId1 = UUID.randomUUID();
    UUID orderId2 = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    List<Order> orders =
        List.of(
            buildOrder(orderId1, customerId, OrderStatus.PENDING, new BigDecimal("100000")),
            buildOrder(orderId2, customerId, OrderStatus.CONFIRMED, new BigDecimal("200000")));

    when(repository.findAll()).thenReturn(orders);

    List<Order> result = useCase.execute();

    assertThat(result).hasSize(2);
    assertThat(result.get(0).getId().getValue()).isEqualTo(orderId1);
    assertThat(result.get(1).getId().getValue()).isEqualTo(orderId2);
  }

  @Test
  void shouldReturnEmptyListWhenNoOrders() {
    when(repository.findAll()).thenReturn(Collections.emptyList());

    List<Order> result = useCase.execute();

    assertThat(result).isEmpty();
  }

  @Test
  void shouldCallRepositoryFindAll() {
    when(repository.findAll()).thenReturn(Collections.emptyList());

    useCase.execute();

    verify(repository).findAll();
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
