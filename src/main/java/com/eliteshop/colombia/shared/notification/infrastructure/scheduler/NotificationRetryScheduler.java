package com.eliteshop.colombia.shared.notification.infrastructure.scheduler;

import com.eliteshop.colombia.shared.notification.application.usecase.RetryPendingNotificationsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationRetryScheduler {

  private final RetryPendingNotificationsUseCase retryUseCase;

  @Scheduled(fixedDelayString = "${slack.queue.retry-interval:60000}")
  public void retryPendingNotifications() {
    retryUseCase.execute();
  }
}
