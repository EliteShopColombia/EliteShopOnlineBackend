package com.eliteshop.colombia.shared.notification.application.usecase;

import com.eliteshop.colombia.shared.notification.domain.exception.NotificationFailedException;
import com.eliteshop.colombia.shared.notification.domain.model.SlackMessage;
import com.eliteshop.colombia.shared.notification.domain.port.NotificationPort;
import com.eliteshop.colombia.shared.notification.domain.port.SlackMessageRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class RetryPendingNotificationsUseCase {

  private final SlackMessageRepository repository;
  private final NotificationPort notificationPort;

  public void execute() {
    log.info("Reintentando notificaciones pendientes");
    List<SlackMessage> pending = repository.findByStatus("PENDING");

    for (SlackMessage message : pending) {
      if (!message.canRetry()) {
        repository.updateStatus(message.id(), "FAILED");
        continue;
      }

      try {
        notificationPort.send(message);
        repository.updateStatus(message.id(), "SENT");
        log.info("Notificación {} enviada exitosamente", message.id());
      } catch (NotificationFailedException e) {
        Instant nextRetry = Instant.now().plus(1, ChronoUnit.MINUTES);
        repository.incrementRetryCount(message.id(), nextRetry.toString());
        log.error("Error al reintentar notificación {}: {}", message.id(), e.getMessage());
      }
    }
    log.info("Reintento de notificaciones completado. Total pendientes: {}", pending.size());
  }
}
