package com.eliteshop.colombia.shared.notification.infrastructure.adapter;

import com.eliteshop.colombia.shared.notification.domain.model.SlackMessage;
import com.eliteshop.colombia.shared.notification.domain.model.SlackMessageId;
import com.eliteshop.colombia.shared.notification.domain.port.SlackMessageRepository;
import com.eliteshop.colombia.shared.notification.infrastructure.persistence.SlackMessageEntity;
import com.eliteshop.colombia.shared.notification.infrastructure.persistence.SlackMessageJpaRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SlackMessagePostgresAdapter implements SlackMessageRepository {

  private final SlackMessageJpaRepository jpaRepository;

  @Override
  public SlackMessage save(SlackMessage message) {
    SlackMessageEntity entity = toEntity(message);
    jpaRepository.save(entity);
    return message;
  }

  @Override
  public void updateStatus(SlackMessageId id, String status) {
    jpaRepository.updateStatus(id.value(), status);
  }

  @Override
  public void incrementRetryCount(SlackMessageId id, String nextRetryAt) {
    jpaRepository.incrementRetryCount(id.value(), Instant.parse(nextRetryAt));
  }

  @Override
  public List<SlackMessage> findByStatus(String status) {
    return jpaRepository.findByStatus(status).stream().map(this::toDomain).toList();
  }

  private SlackMessageEntity toEntity(SlackMessage message) {
    SlackMessageEntity entity = new SlackMessageEntity();
    entity.setId(message.id().value());
    entity.setChannel(message.channel().name());
    entity.setText(message.text());
    entity.setStatus(message.status());
    entity.setRetryCount(message.retryCount());
    entity.setMaxRetries(message.maxRetries());
    entity.setCreatedAt(message.createdAt());
    entity.setNextRetryAt(message.nextRetryAt());
    return entity;
  }

  private SlackMessage toDomain(SlackMessageEntity entity) {
    return new SlackMessage(
        SlackMessageId.of(entity.getId()),
        com.eliteshop.colombia.shared.notification.domain.model.NotificationChannel.valueOf(
            entity.getChannel()),
        entity.getText(),
        entity.getStatus(),
        entity.getRetryCount(),
        entity.getMaxRetries(),
        entity.getCreatedAt(),
        entity.getNextRetryAt());
  }
}
