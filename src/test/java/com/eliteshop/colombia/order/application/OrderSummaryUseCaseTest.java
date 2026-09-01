package com.eliteshop.colombia.order.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.eliteshop.colombia.order.domain.model.*;
import com.eliteshop.colombia.order.domain.repository.OrderItemRepository;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import com.eliteshop.colombia.order.infrastructure.controller.dto.OrderSummaryResponse;
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
class OrderSummaryUseCaseTest {

  @Mock private OrderRepository orderRepository;
  @Mock private OrderItemRepository orderItemRepository;

  private OrderSummaryUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new OrderSummaryUseCase(orderRepository, orderItemRepository);
  }

  @Test
  void shouldReturnSummaryForSellerWithOrders() {
    UUID sellerId = UUID.randomUUID();
    UUID orderId1 = UUID.randomUUID();
    UUID orderId2 = UUID.randomUUID();
    UUID orderId3 = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    when(orderItemRepository.findDistinctOrderIdsBySellerId(sellerId))
        .thenReturn(List.of(orderId1, orderId2, orderId3));

    Order order1 = buildOrder(orderId1, customerId, OrderStatus.PAID, new BigDecimal("100000"));
    Order order2 = buildOrder(orderId2, customerId, OrderStatus.SHIPPED, new BigDecimal("200000"));
    Order order3 =
        buildOrder(orderId3, customerId, OrderStatus.COMPLETED, new BigDecimal("300000"));

    when(orderRepository.findAllByIds(any())).thenReturn(List.of(order1, order2, order3));

    OrderSummaryResponse response = useCase.execute(sellerId);

    assertThat(response.getSellerId()).isEqualTo(sellerId);
    assertThat(response.getTotalOrders()).isEqualTo(3);
    assertThat(response.getTotalRevenue()).isEqualByComparingTo(new BigDecimal("600000"));
    assertThat(response.getAverageOrderValue()).isEqualByComparingTo(new BigDecimal("200000.00"));
    assertThat(response.getOrdersByStatus()).containsEntry("PAID", 1);
    assertThat(response.getOrdersByStatus()).containsEntry("SHIPPED", 1);
    assertThat(response.getOrdersByStatus()).containsEntry("COMPLETED", 1);
  }

  @Test
  void shouldReturnEmptySummaryWhenNoOrders() {
    UUID sellerId = UUID.randomUUID();

    when(orderItemRepository.findDistinctOrderIdsBySellerId(sellerId)).thenReturn(List.of());
    when(orderRepository.findAllByIds(any())).thenReturn(List.of());

    OrderSummaryResponse response = useCase.execute(sellerId);

    assertThat(response.getTotalOrders()).isEqualTo(0);
    assertThat(response.getTotalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(response.getAverageOrderValue()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(response.getOrdersByStatus()).isEmpty();
  }

  @Test
  void shouldCountOrdersByStatusCorrectly() {
    UUID sellerId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    UUID o1 = UUID.randomUUID();
    UUID o2 = UUID.randomUUID();
    UUID o3 = UUID.randomUUID();
    UUID o4 = UUID.randomUUID();

    when(orderItemRepository.findDistinctOrderIdsBySellerId(sellerId))
        .thenReturn(List.of(o1, o2, o3, o4));

    Order order1 = buildOrder(o1, customerId, OrderStatus.PAID, new BigDecimal("100000"));
    Order order2 = buildOrder(o2, customerId, OrderStatus.PAID, new BigDecimal("100000"));
    Order order3 = buildOrder(o3, customerId, OrderStatus.CANCELLED, new BigDecimal("50000"));
    Order order4 = buildOrder(o4, customerId, OrderStatus.PAID, new BigDecimal("150000"));

    when(orderRepository.findAllByIds(any())).thenReturn(List.of(order1, order2, order3, order4));

    OrderSummaryResponse response = useCase.execute(sellerId);

    assertThat(response.getTotalOrders()).isEqualTo(4);
    assertThat(response.getOrdersByStatus()).containsEntry("PAID", 3);
    assertThat(response.getOrdersByStatus()).containsEntry("CANCELLED", 1);
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
