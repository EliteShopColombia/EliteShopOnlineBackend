package com.eliteshop.colombia.shared.notification.infrastructure.listener;

import com.eliteshop.colombia.order.domain.event.OrderStatusChangedEvent;
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
public class OrderStatusChangedNotificationListener {

  private final SlackWebhookAdapter slackWebhookAdapter;

  private static final DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

  private static final String STATUS_TEMPLATE =
      ":package: *Actualizacion de Pedido*\n"
          + "• Pedido: `%s`\n"
          + "• Cliente: `%s`\n"
          + "• Estado: %s → %s\n"
          + "• Hora: %s";

  @Async
  @EventListener
  public void handleOrderStatusChanged(OrderStatusChangedEvent event) {
    log.info(
        "OrderStatusChanged received - orderId={}, {} -> {}",
        event.orderId(),
        event.previousStatus(),
        event.newStatus());

    String statusEmoji = getStatusEmoji(event.newStatus());
    String message =
        String.format(
            STATUS_TEMPLATE,
            event.orderId(),
            event.customerId(),
            event.previousStatus(),
            statusEmoji + " " + event.newStatus(),
            TIME_FORMATTER.format(event.occurredAt()));

    slackWebhookAdapter.sendToChannel("notifications-test", message);

    log.info("Notificacion de cambio de estado enviada para orderId={}", event.orderId());
  }

  private String getStatusEmoji(String status) {
    return switch (status) {
      case "PENDING_PAYMENT" -> ":clock1:";
      case "PAID" -> ":white_check_mark:";
      case "IN_PREPARATION" -> ":package:";
      case "SHIPPED" -> ":truck:";
      case "OUT_FOR_DELIVERY" -> ":motor_scooter:";
      case "DELIVERED" -> ":gift:";
      case "COMPLETED" -> ":tada:";
      case "CANCELLED" -> ":x:";
      case "DISPUTE" -> ":warning:";
      case "REFUNDED" -> ":money_with_wings:";
      default -> ":hourglass_flowing_sand:";
    };
  }
}
