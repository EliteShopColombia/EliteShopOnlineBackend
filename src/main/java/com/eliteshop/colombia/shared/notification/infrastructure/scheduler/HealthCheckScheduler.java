package com.eliteshop.colombia.shared.notification.infrastructure.scheduler;

import com.eliteshop.colombia.shared.notification.application.usecase.SendNotificationUseCase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@ConditionalOnProperty(name = "healthcheck.enabled", havingValue = "true", matchIfMissing = false)
@Component
@RequiredArgsConstructor
public class HealthCheckScheduler {

  private final SendNotificationUseCase sendNotificationUseCase;
  private final WebClient notificationWebClient;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Value("${management.server.port:8080}")
  private String serverPort;

  private boolean lastStatus = true;
  private boolean initialized = false;

  @Scheduled(fixedDelayString = "${healthcheck.interval:30000}")
  public void checkHealth() {
    log.info("HealthCheckScheduler ejecutándose...");
    try {
      String healthUrl = "http://localhost:8080" + "/actuator/health";
      log.info("Consultando: {}", healthUrl);

      String response =
          notificationWebClient.get().uri(healthUrl).retrieve().bodyToMono(String.class).block();

      JsonNode root = objectMapper.readTree(response);
      String status = root.path("status").asText("UNKNOWN");
      log.info("Estado de salud: {}", status);

      boolean isUp = "UP".equals(status);
      log.info("isUp={}, lastStatus={}, initialized={}", isUp, lastStatus, initialized);

      if (!initialized) {
        lastStatus = isUp;
        initialized = true;
        log.info("Primera ejecución, enviando notificación...");
        sendNotification(isUp);
        return;
      }

      if (isUp != lastStatus) {
        lastStatus = isUp;
        log.info("Cambio de estado detectado, enviando notificación...");
        sendNotification(isUp);
      } else {
        log.info("Sin cambio de estado, no se envía notificación");
      }

    } catch (Exception e) {
      log.error("Error al verificar salud del backend: {}", e.getMessage(), e);

      if (lastStatus) {
        lastStatus = false;
        sendNotification(false);
      }
    }
  }

  private void sendNotification(boolean isUp) {
    String timestamp =
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    String status = isUp ? ":white_check_mark: *UP*" : ":x: *DOWN*";
    String message =
        String.format(
            ":rotating_light: *EliteShop Backend - Health Check*\n" + "Estado: %s\n" + "Hora: %s",
            status, timestamp);

    sendNotificationUseCase.execute(message);
  }
}
