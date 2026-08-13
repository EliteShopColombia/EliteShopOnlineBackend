package com.eliteshop.colombia.shared.notification.infrastructure.adapter;

import com.eliteshop.colombia.shared.notification.domain.exception.NotificationFailedException;
import com.eliteshop.colombia.shared.notification.domain.model.SlackMessage;
import com.eliteshop.colombia.shared.notification.domain.port.NotificationPort;
import com.eliteshop.colombia.shared.notification.infrastructure.config.SlackProperties;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlackWebhookAdapter implements NotificationPort {

  private final WebClient notificationWebClient;
  private final SlackProperties properties;

  @Override
  @CircuitBreaker(name = "slack", fallbackMethod = "fallbackSend")
  @Retry(name = "slack")
  public void send(SlackMessage message) {
    log.info("SlackWebhookAdapter.send() - enabled={}", properties.isEnabled());
    if (!properties.isEnabled()) {
      log.warn("Slack notifications disabled");
      return;
    }

    SlackProperties.Channel healthChannel = properties.getChannels().get("health");
    if (healthChannel == null) {
      log.error("Canal 'health' no configurado en slack.channels");
      return;
    }

    Map<String, String> payload =
        Map.of("channel", healthChannel.getChannel(), "text", message.text());

    log.info(
        "Enviando a Slack: channel={}, webhookUrl={}",
        healthChannel.getChannel(),
        healthChannel.getWebhookUrl());

    notificationWebClient
        .post()
        .uri(healthChannel.getWebhookUrl())
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(payload)
        .retrieve()
        .toBodilessEntity()
        .block();

    log.info("Mensaje enviado a Slack exitosamente");
  }

  public void sendToChannel(String channelName, String text) {
    SlackProperties.Channel channelConfig = properties.getChannels().get(channelName);
    if (channelConfig == null) {
      log.error("Canal '{}' no configurado en slack.channels", channelName);
      return;
    }

    Map<String, String> payload = Map.of("channel", channelConfig.getChannel(), "text", text);

    log.info(
        "Enviando a Slack: channel={}, webhookUrl={}",
        channelConfig.getChannel(),
        channelConfig.getWebhookUrl());

    notificationWebClient
        .post()
        .uri(channelConfig.getWebhookUrl())
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(payload)
        .retrieve()
        .toBodilessEntity()
        .block();

    log.info("Mensaje enviado a Slack exitosamente en canal {}", channelName);
  }

  public void fallbackSend(SlackMessage message, Exception e) {
    log.error("Slack webhook falló, ejecutando fallback: {}", e.getClass().getName(), e);
    throw new NotificationFailedException(
        "Slack webhook falló después de reintentos: " + e.getMessage(), e);
  }
}
