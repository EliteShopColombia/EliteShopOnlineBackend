package com.eliteshop.colombia.shared.notification.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(name = "slack_message_queue")
public class SlackMessageEntity {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "channel", nullable = false)
  private String channel;

  @Column(name = "text", nullable = false, columnDefinition = "TEXT")
  private String text;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "retry_count", nullable = false)
  private int retryCount;

  @Column(name = "max_retries", nullable = false)
  private int maxRetries;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "next_retry_at")
  private Instant nextRetryAt;
}
