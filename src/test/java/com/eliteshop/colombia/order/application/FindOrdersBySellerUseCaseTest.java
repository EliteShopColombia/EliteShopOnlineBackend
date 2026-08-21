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
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FindOrdersBySellerUseCaseTest {

  @Mock private OrderRepository orderRepository;
  @Mock private OrderItemRepository orderItemRepository;

  private FindOrdersBySellerUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new FindOrdersBySellerUseCase(orderRepository, orderItemRepository);
  }

  @Test
  void shouldReturnOrdersForSeller() {
    UUID sellerId = UUID.randomUUID();
    UUID orderId1 = UUID.randomUUID();
    UUID orderId2 = UUID.randomUUID();

    when(orderItemRepository.findDistinctOrderIdsBySellerId(sellerId))
        .thenReturn(List.of(orderId1, orderId2));

    Order order1 =
        buildOrder(orderId1, UUID.randomUUID(), OrderStatus.PAID, new BigDecimal("150000"));
    Order order2 =
        buildOrder(orderId2, UUID.randomUUID(), OrderStatus.SHIPPED, new BigDecimal("250000"));

    when(orderRepository.findById(any(OrderId.class)))
        .thenAnswer(
            invocation -> {
              OrderId id = invocation.getArgument(0);
              if (id.getValue().equals(orderId1)) return Optional.of(order1);
              if (id.getValue().equals(orderId2)) return Optional.of(order2);
              return Optional.empty();
            });

    List<Order> result = useCase.execute(sellerId);

    assertThat(result).hasSize(2);
    assertThat(result.get(0).getId().getValue()).isEqualTo(orderId1);
    assertThat(result.get(1).getId().getValue()).isEqualTo(orderId2);
  }

  @Test
  void shouldReturnEmptyListWhenNoOrdersForSeller() {
    UUID sellerId = UUID.randomUUID();

    when(orderItemRepository.findDistinctOrderIdsBySellerId(sellerId)).thenReturn(List.of());

    List<Order> result = useCase.execute(sellerId);

    assertThat(result).isEmpty();
  }

  @Test
  void shouldHandleMissingOrderGracefully() {
    UUID sellerId = UUID.randomUUID();
    UUID orderId1 = UUID.randomUUID();
    UUID orderId2 = UUID.randomUUID();

    when(orderItemRepository.findDistinctOrderIdsBySellerId(sellerId))
        .thenReturn(List.of(orderId1, orderId2));

    Order order1 =
        buildOrder(orderId1, UUID.randomUUID(), OrderStatus.PAID, new BigDecimal("150000"));

    when(orderRepository.findById(any(OrderId.class)))
        .thenAnswer(
            invocation -> {
              OrderId id = invocation.getArgument(0);
              if (id.getValue().equals(orderId1)) return Optional.of(order1);
              return Optional.empty();
            });

    List<Order> result = useCase.execute(sellerId);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId().getValue()).isEqualTo(orderId1);
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
