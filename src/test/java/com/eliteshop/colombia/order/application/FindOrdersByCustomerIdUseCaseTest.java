package com.eliteshop.colombia.order.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.eliteshop.colombia.order.domain.model.*;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import com.eliteshop.colombia.shared.domain.PageResult;
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

    Order order1 = buildOrder(orderId1, customerId, OrderStatus.PAID, new BigDecimal("150000"));
    Order order2 = buildOrder(orderId2, customerId, OrderStatus.SHIPPED, new BigDecimal("250000"));

    PageResult<Order> pageResult = PageResult.of(List.of(order1, order2), 0, 25, 2);
    when(repository.findPageByCustomerId(eq(customerId), eq(0), eq(25))).thenReturn(pageResult);

    PageResult<Order> result = useCase.execute(new OrderCustomerId(customerId), 0, 25);

    assertThat(result.content()).hasSize(2);
    assertThat(result.content().get(0).getId().getValue()).isEqualTo(orderId1);
    assertThat(result.content().get(1).getId().getValue()).isEqualTo(orderId2);
    assertThat(result.totalElements()).isEqualTo(2);
  }

  @Test
  void shouldReturnEmptyListWhenNoOrdersForCustomer() {
    UUID customerId = UUID.randomUUID();

    PageResult<Order> pageResult = PageResult.of(List.of(), 0, 25, 0);
    when(repository.findPageByCustomerId(eq(customerId), eq(0), eq(25))).thenReturn(pageResult);

    PageResult<Order> result = useCase.execute(new OrderCustomerId(customerId), 0, 25);

    assertThat(result.content()).isEmpty();
    assertThat(result.totalElements()).isEqualTo(0);
  }

  @Test
  void shouldCallRepositoryWithCorrectCustomerId() {
    UUID customerId = UUID.randomUUID();

    PageResult<Order> pageResult = PageResult.of(List.of(), 0, 25, 0);
    when(repository.findPageByCustomerId(eq(customerId), eq(0), eq(25))).thenReturn(pageResult);

    useCase.execute(new OrderCustomerId(customerId), 0, 25);

    verify(repository).findPageByCustomerId(customerId, 0, 25);
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
