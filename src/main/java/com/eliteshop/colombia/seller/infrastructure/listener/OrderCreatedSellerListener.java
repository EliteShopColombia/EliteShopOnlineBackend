package com.eliteshop.colombia.seller.infrastructure.listener;

import com.eliteshop.colombia.order.domain.event.OrderCreatedEvent;
import com.eliteshop.colombia.shared.notification.infrastructure.adapter.SlackWebhookAdapter;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedSellerListener {

  private final SlackWebhookAdapter slackWebhookAdapter;

  private static final DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

  @Async
  @EventListener
  public void handleOrderCreated(OrderCreatedEvent event) {
    log.info(
        "OrderCreated received for seller notification - orderId={}, customerId={}",
        event.orderId(),
        event.customerId());

    String message =
        String.format(
            ":shopping_bags: *Nuevo Pedido Recibido*\n"
                + "• Pedido: `%s`\n"
                + "• Cliente: `%s`\n"
                + "• Total: $%s\n"
                + "• Hora: %s",
            event.orderId(),
            event.customerId(),
            event.totalAmount(),
            TIME_FORMATTER.format(event.occurredAt()));

    slackWebhookAdapter.sendToChannel("pedidos-test", message);

    log.info(
        "Notificación de pedido enviada al canal pedidos-test para orderId={}", event.orderId());
  }
}
