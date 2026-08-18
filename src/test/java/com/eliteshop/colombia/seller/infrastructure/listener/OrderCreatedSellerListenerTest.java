package com.eliteshop.colombia.seller.infrastructure.listener;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.eliteshop.colombia.order.domain.event.OrderCreatedEvent;
import com.eliteshop.colombia.shared.notification.infrastructure.adapter.SlackWebhookAdapter;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderCreatedSellerListenerTest {

  @Mock private SlackWebhookAdapter slackWebhookAdapter;

  private OrderCreatedSellerListener listener;

  @BeforeEach
  void setUp() {
    listener = new OrderCreatedSellerListener(slackWebhookAdapter);
  }

  @Test
  void shouldSendSlackNotificationWhenOrderCreated() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    OrderCreatedEvent event =
        new OrderCreatedEvent(
            orderId, customerId, new BigDecimal("350000"), java.time.Instant.now());

    listener.handleOrderCreated(event);

    verify(slackWebhookAdapter).sendToChannel(eq("pedidos-test"), anyString());
  }

  @Test
  void shouldIncludeOrderIdInNotification() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    OrderCreatedEvent event =
        new OrderCreatedEvent(
            orderId, customerId, new BigDecimal("200000"), java.time.Instant.now());

    listener.handleOrderCreated(event);

    verify(slackWebhookAdapter)
        .sendToChannel(
            eq("pedidos-test"),
            argThat(
                message ->
                    message.contains(orderId.toString())
                        && message.contains(customerId.toString())
                        && message.contains("200000")));
  }
}
