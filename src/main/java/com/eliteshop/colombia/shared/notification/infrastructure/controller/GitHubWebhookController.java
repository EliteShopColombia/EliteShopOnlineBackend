package com.eliteshop.colombia.shared.notification.infrastructure.controller;

import com.eliteshop.colombia.shared.notification.infrastructure.adapter.SlackWebhookAdapter;
import com.eliteshop.colombia.shared.notification.infrastructure.config.GitHubWebhookProperties;
import com.eliteshop.colombia.shared.notification.infrastructure.controller.dto.GitHubWebhookPayload;
import com.eliteshop.colombia.shared.notification.infrastructure.mapper.GitHubPullRequestMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
public class GitHubWebhookController {

  private final SlackWebhookAdapter slackWebhookAdapter;
  private final GitHubWebhookProperties properties;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public GitHubWebhookController(
      SlackWebhookAdapter slackWebhookAdapter, GitHubWebhookProperties properties) {
    this.slackWebhookAdapter = slackWebhookAdapter;
    this.properties = properties;
  }

  @PostMapping("/github")
  public ResponseEntity<String> handleGitHubWebhook(
      @RequestHeader("X-GitHub-Event") String event,
      @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature,
      @RequestBody String payload) {

    log.info("Webhook recibido de GitHub - evento: {}", event);

    if (!"pull_request".equals(event)) {
      log.info("Evento ignorado: {}", event);
      return ResponseEntity.ok("Evento ignorado: " + event);
    }

    if (properties.isSecretConfigured()) {
      if (!WebhookSignatureValidator.isValid(payload, signature, properties.getSecret())) {
        log.warn("Firma HMAC inválida - webhook rechazado");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Firma inválida");
      }
    }

    try {
      GitHubWebhookPayload prEvent = objectMapper.readValue(payload, GitHubWebhookPayload.class);

      if (!List.of("opened", "closed", "reopened", "synchronize").contains(prEvent.getAction())) {
        log.info("Acción de PR ignorada: {}", prEvent.getAction());
        return ResponseEntity.ok("Acción ignorada: " + prEvent.getAction());
      }

      String message = GitHubPullRequestMapper.toSlackMessage(prEvent);
      slackWebhookAdapter.sendToChannel("github", message);

      log.info("Notificación de PR #{} enviada a Slack", prEvent.getPullRequest().getNumber());
      return ResponseEntity.ok("Notificación enviada");

    } catch (Exception e) {
      log.error("Error procesando webhook de GitHub: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error procesando webhook");
    }
  }
}
