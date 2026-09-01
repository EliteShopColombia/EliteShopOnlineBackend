package com.eliteshop.colombia.shared.notification.infrastructure.listener;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.eliteshop.colombia.order.domain.event.OrderStatusChangedEvent;
import com.eliteshop.colombia.shared.notification.infrastructure.adapter.SlackWebhookAdapter;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderStatusChangedNotificationListenerTest {

  @Mock private SlackWebhookAdapter slackWebhookAdapter;

  private OrderStatusChangedNotificationListener listener;

  @BeforeEach
  void setUp() {
    listener = new OrderStatusChangedNotificationListener(slackWebhookAdapter);
  }

  @Test
  void shouldSendSlackNotificationOnStatusChange() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    OrderStatusChangedEvent event =
        new OrderStatusChangedEvent(
            orderId, customerId, "PENDING_PAYMENT", "PAID", java.time.Instant.now());

    listener.handleOrderStatusChanged(event);

    verify(slackWebhookAdapter).sendToChannel(eq("notifications-test"), anyString());
  }

  @Test
  void shouldIncludeStatusTransitionInNotification() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    OrderStatusChangedEvent event =
        new OrderStatusChangedEvent(
            orderId, customerId, "PENDING_PAYMENT", "SHIPPED", java.time.Instant.now());

    listener.handleOrderStatusChanged(event);

    verify(slackWebhookAdapter)
        .sendToChannel(
            eq("notifications-test"),
            argThat(
                message ->
                    message.contains(orderId.toString())
                        && message.contains(customerId.toString())
                        && message.contains("PENDING_PAYMENT")
                        && message.contains("SHIPPED")));
  }

  @Test
  void shouldHandleAllStatusTransitions() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    String[] statuses = {"PENDING_PAYMENT", "PAID", "SHIPPED", "DELIVERED", "CANCELLED"};

    for (int i = 0; i < statuses.length - 1; i++) {
      reset(slackWebhookAdapter);

      OrderStatusChangedEvent event =
          new OrderStatusChangedEvent(
              orderId, customerId, statuses[i], statuses[i + 1], java.time.Instant.now());

      listener.handleOrderStatusChanged(event);

      verify(slackWebhookAdapter).sendToChannel(eq("notifications-test"), anyString());
    }
  }
}
