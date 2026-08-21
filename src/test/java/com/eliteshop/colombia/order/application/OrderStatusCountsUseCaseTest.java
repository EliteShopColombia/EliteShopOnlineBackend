package com.eliteshop.colombia.order.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.eliteshop.colombia.order.domain.model.*;
import com.eliteshop.colombia.order.domain.repository.OrderItemRepository;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import com.eliteshop.colombia.order.infrastructure.controller.dto.OrderStatusCountResponse;
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
class OrderStatusCountsUseCaseTest {

  @Mock private OrderRepository orderRepository;
  @Mock private OrderItemRepository orderItemRepository;

  private OrderStatusCountsUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new OrderStatusCountsUseCase(orderRepository, orderItemRepository);
  }

  @Test
  void shouldReturnCorrectCountsByStatus() {
    UUID sellerId = UUID.randomUUID();
    UUID o1 = UUID.randomUUID();
    UUID o2 = UUID.randomUUID();
    UUID o3 = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    when(orderItemRepository.findDistinctOrderIdsBySellerId(sellerId))
        .thenReturn(List.of(o1, o2, o3));

    when(orderRepository.findById(any(OrderId.class)))
        .thenAnswer(
            invocation -> {
              OrderId id = invocation.getArgument(0);
              if (id.getValue().equals(o1))
                return Optional.of(
                    buildOrder(o1, customerId, OrderStatus.PAID, new BigDecimal("100000")));
              if (id.getValue().equals(o2))
                return Optional.of(
                    buildOrder(o2, customerId, OrderStatus.PAID, new BigDecimal("200000")));
              if (id.getValue().equals(o3))
                return Optional.of(
                    buildOrder(o3, customerId, OrderStatus.SHIPPED, new BigDecimal("150000")));
              return Optional.empty();
            });

    OrderStatusCountResponse response = useCase.execute(sellerId);

    assertThat(response.getSellerId()).isEqualTo(sellerId);
    assertThat(response.getTotalOrders()).isEqualTo(3);
    assertThat(response.getCounts().get("PAID")).isEqualTo(2);
    assertThat(response.getCounts().get("SHIPPED")).isEqualTo(1);
    assertThat(response.getCounts().get("PENDING_PAYMENT")).isEqualTo(0);
    assertThat(response.getCounts().get("DELIVERED")).isEqualTo(0);
  }

  @Test
  void shouldReturnAllZerosWhenNoOrders() {
    UUID sellerId = UUID.randomUUID();

    when(orderItemRepository.findDistinctOrderIdsBySellerId(sellerId)).thenReturn(List.of());

    OrderStatusCountResponse response = useCase.execute(sellerId);

    assertThat(response.getTotalOrders()).isEqualTo(0);
    response.getCounts().values().forEach(count -> assertThat(count).isEqualTo(0));
  }

  @Test
  void shouldIncludeAllStatusesInResponse() {
    UUID sellerId = UUID.randomUUID();

    when(orderItemRepository.findDistinctOrderIdsBySellerId(sellerId)).thenReturn(List.of());

    OrderStatusCountResponse response = useCase.execute(sellerId);

    for (OrderStatus status : OrderStatus.values()) {
      assertThat(response.getCounts()).containsKey(status.name());
    }
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
