package com.eliteshop.colombia.shared.notification.infrastructure.config;

import com.eliteshop.colombia.shared.notification.application.usecase.RetryPendingNotificationsUseCase;
import com.eliteshop.colombia.shared.notification.application.usecase.SendNotificationUseCase;
import com.eliteshop.colombia.shared.notification.domain.port.NotificationPort;
import com.eliteshop.colombia.shared.notification.domain.port.SlackMessageRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class NotificationBeanConfiguration {

  @Bean
  public WebClient notificationWebClient() {
    return WebClient.builder().build();
  }

  @Bean
  public SendNotificationUseCase sendNotificationUseCase(
          NotificationPort notificationPort, SlackMessageRepository repository) {
    return new SendNotificationUseCase(notificationPort, repository);
  }

  @Bean
  public RetryPendingNotificationsUseCase retryPendingNotificationsUseCase(
          SlackMessageRepository repository, NotificationPort notificationPort) {
    return new RetryPendingNotificationsUseCase(repository, notificationPort);
  }
}