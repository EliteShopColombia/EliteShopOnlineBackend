package com.eliteshop.colombia.order.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.eliteshop.colombia.order.domain.model.*;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FindOrdersByCustomerIdUseCaseTest {

  @Mock private OrderRepository repository;

  private FindOrdersByCustomerIdUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new FindOrdersByCustomerIdUseCase(repository);
  }

  @Test
  void shouldReturnOrdersForCustomer() {
    UUID customerId = UUID.randomUUID();
    UUID orderId1 = UUID.randomUUID();
    UUID orderId2 = UUID.randomUUID();

    List<Order> orders =
        List.of(
            buildOrder(orderId1, customerId, OrderStatus.PAID, new BigDecimal("150000")),
            buildOrder(orderId2, customerId, OrderStatus.SHIPPED, new BigDecimal("250000")));

    when(repository.findByCustomerId(customerId)).thenReturn(orders);

    List<Order> result = useCase.execute(new OrderCustomerId(customerId));

    assertThat(result).hasSize(2);
    assertThat(result.get(0).getId().getValue()).isEqualTo(orderId1);
    assertThat(result.get(1).getId().getValue()).isEqualTo(orderId2);
  }

  @Test
  void shouldReturnEmptyListWhenNoOrdersForCustomer() {
    UUID customerId = UUID.randomUUID();

    when(repository.findByCustomerId(customerId)).thenReturn(List.of());

    List<Order> result = useCase.execute(new OrderCustomerId(customerId));

    assertThat(result).isEmpty();
  }

  @Test
  void shouldCallRepositoryWithCorrectCustomerId() {
    UUID customerId = UUID.randomUUID();

    when(repository.findByCustomerId(customerId)).thenReturn(List.of());

    useCase.execute(new OrderCustomerId(customerId));

    verify(repository).findByCustomerId(customerId);
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
