package com.eliteshop.colombia.shared.notification.infrastructure.config;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "slack")
public class SlackProperties {

  private boolean enabled;
  private Map<String, Channel> channels = new HashMap<>();
  private Queue queue = new Queue();

  @Data
  public static class Channel {

    private String webhookUrl;
    private String channel;
  }

  @Data
  public static class Queue {

    private boolean enabled = true;
    private int maxRetries = 5;
    private long retryInterval = 60000;
  }
}
