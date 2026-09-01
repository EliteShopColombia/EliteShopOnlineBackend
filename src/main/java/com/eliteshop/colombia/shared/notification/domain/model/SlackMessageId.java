package com.eliteshop.colombia.shared.notification.domain.model;

import java.util.UUID;

public record SlackMessageId(UUID value) {

  public static SlackMessageId generate() {
    return new SlackMessageId(UUID.randomUUID());
  }

  public static SlackMessageId of(UUID value) {
    return new SlackMessageId(value);
  }
}
