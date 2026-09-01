package com.eliteshop.colombia.order.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.eliteshop.colombia.order.domain.model.OrderId;
import com.eliteshop.colombia.order.domain.model.tracking.TrackingEvent;
import com.eliteshop.colombia.order.domain.repository.TrackingEventRepository;
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
class GetTrackingEventsUseCaseTest {

  @Mock private TrackingEventRepository trackingEventRepository;

  private GetTrackingEventsUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new GetTrackingEventsUseCase(trackingEventRepository);
  }

  @Test
  void shouldReturnTrackingEventsForOrder() {
    UUID orderId = UUID.randomUUID();

    List<TrackingEvent> events =
        List.of(
            TrackingEvent.create(
                orderId, "RECEIVED", "Bogota", "Package received", Timestamp.from(Instant.now())),
            TrackingEvent.create(
                orderId, "IN_TRANSIT", "Bogota", "In transit", Timestamp.from(Instant.now())));

    when(trackingEventRepository.findByOrderId(orderId)).thenReturn(events);

    List<TrackingEvent> result = useCase.execute(new OrderId(orderId));

    assertThat(result).hasSize(2);
  }

  @Test
  void shouldReturnEmptyListWhenNoEvents() {
    UUID orderId = UUID.randomUUID();

    when(trackingEventRepository.findByOrderId(orderId)).thenReturn(List.of());

    List<TrackingEvent> result = useCase.execute(new OrderId(orderId));

    assertThat(result).isEmpty();
  }

  @Test
  void shouldCallRepositoryWithCorrectOrderId() {
    UUID orderId = UUID.randomUUID();

    when(trackingEventRepository.findByOrderId(orderId)).thenReturn(List.of());

    useCase.execute(new OrderId(orderId));

    verify(trackingEventRepository).findByOrderId(orderId);
  }
}
