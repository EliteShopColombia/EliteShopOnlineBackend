package com.eliteshop.colombia.shared.notification.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "github.webhook")
public class GitHubWebhookProperties {

  private String secret;
  private boolean enabled = true;

  public boolean isSecretConfigured() {
    return secret != null && !secret.isBlank();
  }
}
