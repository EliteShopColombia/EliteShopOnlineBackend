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
class FindOrdersBySellerUseCaseTest {

  @Mock private OrderRepository orderRepository;

  private FindOrdersBySellerUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new FindOrdersBySellerUseCase(orderRepository);
  }

  @Test
  void shouldReturnOrdersForSeller() {
    UUID sellerId = UUID.randomUUID();
    UUID orderId1 = UUID.randomUUID();
    UUID orderId2 = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    Order order1 = buildOrder(orderId1, customerId, OrderStatus.PAID, new BigDecimal("150000"));
    Order order2 = buildOrder(orderId2, customerId, OrderStatus.SHIPPED, new BigDecimal("250000"));

    PageResult<Order> pageResult = PageResult.of(List.of(order1, order2), 0, 25, 2);
    when(orderRepository.findPageBySellerId(eq(sellerId), eq(0), eq(25))).thenReturn(pageResult);

    PageResult<Order> result = useCase.execute(sellerId, 0, 25);

    assertThat(result.content()).hasSize(2);
    assertThat(result.content().get(0).getId().getValue()).isEqualTo(orderId1);
    assertThat(result.content().get(1).getId().getValue()).isEqualTo(orderId2);
  }

  @Test
  void shouldReturnEmptyListWhenNoOrdersForSeller() {
    UUID sellerId = UUID.randomUUID();

    PageResult<Order> pageResult = PageResult.of(List.of(), 0, 25, 0);
    when(orderRepository.findPageBySellerId(eq(sellerId), eq(0), eq(25))).thenReturn(pageResult);

    PageResult<Order> result = useCase.execute(sellerId, 0, 25);

    assertThat(result.content()).isEmpty();
    assertThat(result.totalElements()).isEqualTo(0);
  }

  @Test
  void shouldHandleMissingOrderGracefully() {
    UUID sellerId = UUID.randomUUID();
    UUID orderId1 = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    Order order1 = buildOrder(orderId1, customerId, OrderStatus.PAID, new BigDecimal("150000"));

    PageResult<Order> pageResult = PageResult.of(List.of(order1), 0, 25, 1);
    when(orderRepository.findPageBySellerId(eq(sellerId), eq(0), eq(25))).thenReturn(pageResult);

    PageResult<Order> result = useCase.execute(sellerId, 0, 25);

    assertThat(result.content()).hasSize(1);
    assertThat(result.content().get(0).getId().getValue()).isEqualTo(orderId1);
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
