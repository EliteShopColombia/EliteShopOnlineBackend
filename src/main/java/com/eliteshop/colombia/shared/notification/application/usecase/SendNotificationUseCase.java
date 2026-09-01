package com.eliteshop.colombia.shared.notification.application.usecase;

import com.eliteshop.colombia.shared.notification.domain.exception.NotificationFailedException;
import com.eliteshop.colombia.shared.notification.domain.model.SlackMessage;
import com.eliteshop.colombia.shared.notification.domain.port.NotificationPort;
import com.eliteshop.colombia.shared.notification.domain.port.SlackMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SendNotificationUseCase {

  private final NotificationPort notificationPort;
  private final SlackMessageRepository repository;

  public void execute(String text) {
    log.info("Enviando notificación: {}", text);
    SlackMessage message = SlackMessage.create(text);
    repository.save(message);

    try {
      notificationPort.send(message);
      repository.updateStatus(message.id(), "SENT");
      log.info("Notificación {} enviada exitosamente", message.id());
    } catch (NotificationFailedException e) {
      repository.updateStatus(message.id(), "PENDING");
      log.error("Error al enviar notificación {}: {}", message.id(), e.getMessage());
    }
  }
}
