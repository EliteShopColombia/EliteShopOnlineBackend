package com.eliteshop.colombia.shared.notification.application.usecase;

import com.eliteshop.colombia.shared.notification.domain.exception.NotificationFailedException;
import com.eliteshop.colombia.shared.notification.domain.model.SlackMessage;
import com.eliteshop.colombia.shared.notification.domain.port.NotificationPort;
import com.eliteshop.colombia.shared.notification.domain.port.SlackMessageRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SendNotificationUseCase {

  private final NotificationPort notificationPort;
  private final SlackMessageRepository repository;

  public void execute(String text) {
    SlackMessage message = SlackMessage.create(text);
    repository.save(message);

    try {
      notificationPort.send(message);
      repository.updateStatus(message.id(), "SENT");
    } catch (NotificationFailedException e) {
      repository.updateStatus(message.id(), "PENDING");
    }
  }
}
