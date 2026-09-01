package com.eliteshop.colombia.shared.notification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.eliteshop.colombia.shared.notification.application.usecase.RetryPendingNotificationsUseCase;
import com.eliteshop.colombia.shared.notification.application.usecase.SendNotificationUseCase;
import com.eliteshop.colombia.shared.notification.domain.exception.NotificationFailedException;
import com.eliteshop.colombia.shared.notification.domain.model.NotificationChannel;
import com.eliteshop.colombia.shared.notification.domain.model.SlackMessage;
import com.eliteshop.colombia.shared.notification.domain.model.SlackMessageId;
import com.eliteshop.colombia.shared.notification.domain.port.NotificationPort;
import com.eliteshop.colombia.shared.notification.domain.port.SlackMessageRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationRetryTest {

  @Mock private NotificationPort notificationPort;
  @Mock private SlackMessageRepository repository;
  private SendNotificationUseCase sendNotificationUseCase;
  private RetryPendingNotificationsUseCase retryPendingNotificationsUseCase;

  @BeforeEach
  void setUp() {
    sendNotificationUseCase = new SendNotificationUseCase(notificationPort, repository);
    retryPendingNotificationsUseCase =
        new RetryPendingNotificationsUseCase(repository, notificationPort);
  }

  @Test
  void shouldMarkNotificationAsSent_whenSendSucceeds() {
    sendNotificationUseCase.execute("Test notification");

    verify(repository, times(1)).save(any());
    verify(notificationPort, times(1)).send(any());
    verify(repository).updateStatus(any(), eq("SENT"));
  }

  @Test
  void shouldMarkNotificationAsPending_whenSendFails() {
    doThrow(
            new NotificationFailedException(
                "Connection failed", new RuntimeException("connection refused")))
        .when(notificationPort)
        .send(any());

    sendNotificationUseCase.execute("Test notification");

    verify(repository).updateStatus(any(), eq("PENDING"));
  }

  @Test
  void shouldRetryMessages_whenBelowMaxRetries() {
    SlackMessage message =
        new SlackMessage(
            SlackMessageId.generate(),
            NotificationChannel.SLACK,
            "test",
            "PENDING",
            2,
            5,
            Instant.now(),
            Instant.now());

    when(repository.findByStatus("PENDING")).thenReturn(List.of(message));

    retryPendingNotificationsUseCase.execute();

    verify(notificationPort).send(message);
    verify(repository, never()).updateStatus(message.id(), "FAILED");
  }

  @Test
  void shouldMarkAsFailed_whenMaxRetriesExceeded() {
    SlackMessage message =
        new SlackMessage(
            SlackMessageId.generate(),
            NotificationChannel.SLACK,
            "test",
            "PENDING",
            5,
            5,
            Instant.now(),
            Instant.now());

    when(repository.findByStatus("PENDING")).thenReturn(List.of(message));

    retryPendingNotificationsUseCase.execute();

    verify(notificationPort, never()).send(any());
    verify(repository).updateStatus(message.id(), "FAILED");
  }

  @Test
  void shouldMarkAsSent_whenRetrySucceeds() {
    SlackMessage message =
        new SlackMessage(
            SlackMessageId.generate(),
            NotificationChannel.SLACK,
            "test",
            "PENDING",
            1,
            5,
            Instant.now(),
            Instant.now());

    when(repository.findByStatus("PENDING")).thenReturn(List.of(message));

    retryPendingNotificationsUseCase.execute();

    verify(notificationPort).send(message);
    verify(repository).updateStatus(message.id(), "SENT");
  }

  @Test
  void shouldIncrementRetryCount_whenRetryFails() {
    SlackMessage message =
        new SlackMessage(
            SlackMessageId.generate(),
            NotificationChannel.SLACK,
            "test",
            "PENDING",
            0,
            5,
            Instant.now(),
            Instant.now());

    when(repository.findByStatus("PENDING")).thenReturn(List.of(message));
    doThrow(new NotificationFailedException("timeout", new RuntimeException("timeout occurred")))
        .when(notificationPort)
        .send(any());

    retryPendingNotificationsUseCase.execute();

    verify(repository).incrementRetryCount(any(), anyString());
  }

  @Test
  void shouldAllowRetry_whenBelowMaxRetries() {
    SlackMessage message =
        new SlackMessage(
            SlackMessageId.generate(),
            NotificationChannel.SLACK,
            "test",
            "PENDING",
            3,
            5,
            Instant.now(),
            Instant.now());

    assertThat(message.canRetry()).isTrue();
  }

  @Test
  void shouldDenyRetry_whenAtMaxRetries() {
    SlackMessage message =
        new SlackMessage(
            SlackMessageId.generate(),
            NotificationChannel.SLACK,
            "test",
            "PENDING",
            5,
            5,
            Instant.now(),
            Instant.now());

    assertThat(message.canRetry()).isFalse();
  }
}
