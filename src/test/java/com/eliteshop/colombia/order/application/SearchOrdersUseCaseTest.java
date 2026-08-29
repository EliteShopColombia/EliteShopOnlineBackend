package com.eliteshop.colombia.order.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.eliteshop.colombia.order.domain.model.*;
import com.eliteshop.colombia.order.domain.repository.OrderItemRepository;
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
class SearchOrdersUseCaseTest {

  @Mock private OrderRepository orderRepository;
  @Mock private OrderItemRepository orderItemRepository;

  private SearchOrdersUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new SearchOrdersUseCase(orderRepository, orderItemRepository);
  }

  @Test
  void shouldReturnAllOrdersWhenNoStatusFilter() {
    UUID sellerId = UUID.randomUUID();
    UUID o1 = UUID.randomUUID();
    UUID o2 = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    when(orderItemRepository.findDistinctOrderIdsBySellerId(sellerId)).thenReturn(List.of(o1, o2));

    Order order1 = buildOrder(o1, customerId, OrderStatus.PAID, new BigDecimal("100000"));
    Order order2 = buildOrder(o2, customerId, OrderStatus.SHIPPED, new BigDecimal("200000"));

    when(orderRepository.findAllByIds(any())).thenReturn(List.of(order1, order2));

    SearchOrdersUseCase.SearchResult result = useCase.execute(sellerId, null, 0, 10);

    assertThat(result.total()).isEqualTo(2);
    assertThat(result.orders()).hasSize(2);
    assertThat(result.page()).isEqualTo(0);
    assertThat(result.size()).isEqualTo(10);
    assertThat(result.totalPages()).isEqualTo(1);
  }

  @Test
  void shouldFilterByStatus() {
    UUID sellerId = UUID.randomUUID();
    UUID o1 = UUID.randomUUID();
    UUID o2 = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    when(orderItemRepository.findDistinctOrderIdsBySellerId(sellerId)).thenReturn(List.of(o1, o2));

    Order order1 = buildOrder(o1, customerId, OrderStatus.PAID, new BigDecimal("100000"));
    Order order2 = buildOrder(o2, customerId, OrderStatus.SHIPPED, new BigDecimal("200000"));

    when(orderRepository.findAllByIds(any())).thenReturn(List.of(order1, order2));

    SearchOrdersUseCase.SearchResult result = useCase.execute(sellerId, OrderStatus.PAID, 0, 10);

    assertThat(result.total()).isEqualTo(1);
    assertThat(result.orders()).hasSize(1);
    assertThat(result.orders().get(0).getStatus()).isEqualTo(OrderStatus.PAID);
  }

  @Test
  void shouldPaginateCorrectly() {
    UUID sellerId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    List<UUID> orderIds = new java.util.ArrayList<>();
    for (int i = 0; i < 25; i++) {
      orderIds.add(UUID.randomUUID());
    }

    when(orderItemRepository.findDistinctOrderIdsBySellerId(sellerId)).thenReturn(orderIds);

    List<Order> allOrders =
        orderIds.stream()
            .map(id -> buildOrder(id, customerId, OrderStatus.PAID, new BigDecimal("100000")))
            .toList();

    when(orderRepository.findAllByIds(any())).thenReturn(allOrders);

    SearchOrdersUseCase.SearchResult page0 = useCase.execute(sellerId, null, 0, 10);
    assertThat(page0.orders()).hasSize(10);
    assertThat(page0.total()).isEqualTo(25);
    assertThat(page0.totalPages()).isEqualTo(3);
    assertThat(page0.page()).isEqualTo(0);

    SearchOrdersUseCase.SearchResult page2 = useCase.execute(sellerId, null, 2, 10);
    assertThat(page2.orders()).hasSize(5);
    assertThat(page2.page()).isEqualTo(2);
  }

  @Test
  void shouldReturnEmptyWhenNoOrdersForSeller() {
    UUID sellerId = UUID.randomUUID();

    when(orderItemRepository.findDistinctOrderIdsBySellerId(sellerId)).thenReturn(List.of());
    when(orderRepository.findAllByIds(any())).thenReturn(List.of());

    SearchOrdersUseCase.SearchResult result = useCase.execute(sellerId, null, 0, 10);

    assertThat(result.total()).isEqualTo(0);
    assertThat(result.orders()).isEmpty();
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
