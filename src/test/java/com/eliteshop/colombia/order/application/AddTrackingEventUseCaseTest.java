package com.eliteshop.colombia.order.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.eliteshop.colombia.order.domain.exception.OrderNotFoundException;
import com.eliteshop.colombia.order.domain.model.*;
import com.eliteshop.colombia.order.domain.model.tracking.TrackingEvent;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import com.eliteshop.colombia.order.domain.repository.TrackingEventRepository;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AddTrackingEventUseCaseTest {

  @Mock private TrackingEventRepository trackingEventRepository;
  @Mock private OrderRepository orderRepository;

  private AddTrackingEventUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new AddTrackingEventUseCase(trackingEventRepository, orderRepository);
  }

  @Test
  void shouldAddTrackingEventToExistingOrder() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    Order order =
        new Order(
            new OrderId(orderId),
            new OrderCustomerId(customerId),
            OrderStatus.SHIPPED,
            new OrderTotalAmount(new BigDecimal("200000")),
            new OrderShippingAddress("Calle 100"),
            new OrderShippingDepartment("Bogota"),
            new OrderShippingCity("Bogota"),
            new OrderCreatedAt(Timestamp.from(Instant.now())),
            null,
            null,
            null,
            null);

    when(orderRepository.findById(any(OrderId.class))).thenReturn(Optional.of(order));

    TrackingEvent savedEvent =
        TrackingEvent.create(
            orderId, "IN_TRANSIT", "Bogota", "Package in transit", Timestamp.from(Instant.now()));
    when(trackingEventRepository.save(any(TrackingEvent.class))).thenReturn(savedEvent);

    TrackingEvent result =
        useCase.execute(
            new OrderId(orderId),
            "IN_TRANSIT",
            "Bogota",
            "Package in transit",
            Timestamp.from(Instant.now()));

    assertThat(result).isNotNull();
    verify(trackingEventRepository).save(any(TrackingEvent.class));
  }

  @Test
  void shouldThrowWhenOrderNotFound() {
    UUID orderId = UUID.randomUUID();

    when(orderRepository.findById(any(OrderId.class))).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                useCase.execute(
                    new OrderId(orderId),
                    "IN_TRANSIT",
                    "Bogota",
                    "desc",
                    Timestamp.from(Instant.now())))
        .isInstanceOf(OrderNotFoundException.class);

    verify(trackingEventRepository, never()).save(any());
  }

  @Test
  void shouldSaveEventWithCorrectData() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    Order order =
        new Order(
            new OrderId(orderId),
            new OrderCustomerId(customerId),
            OrderStatus.SHIPPED,
            new OrderTotalAmount(new BigDecimal("200000")),
            new OrderShippingAddress("Calle 100"),
            new OrderShippingDepartment("Bogota"),
            new OrderShippingCity("Bogota"),
            new OrderCreatedAt(Timestamp.from(Instant.now())),
            null,
            null,
            null,
            null);

    when(orderRepository.findById(any(OrderId.class))).thenReturn(Optional.of(order));

    Timestamp eventTime = Timestamp.from(Instant.now());
    TrackingEvent savedEvent =
        TrackingEvent.create(orderId, "DELIVERED", "Medellin", "Delivered to door", eventTime);
    when(trackingEventRepository.save(any(TrackingEvent.class))).thenReturn(savedEvent);

    useCase.execute(new OrderId(orderId), "DELIVERED", "Medellin", "Delivered to door", eventTime);

    ArgumentCaptor<TrackingEvent> captor = ArgumentCaptor.forClass(TrackingEvent.class);
    verify(trackingEventRepository).save(captor.capture());
    assertThat(captor.getValue().getOrderId().getValue()).isEqualTo(orderId);
    assertThat(captor.getValue().getStatus().name()).isEqualTo("DELIVERED");
    assertThat(captor.getValue().getLocation()).isEqualTo("Medellin");
    assertThat(captor.getValue().getDescription()).isEqualTo("Delivered to door");
  }
}
