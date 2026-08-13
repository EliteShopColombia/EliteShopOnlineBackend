package com.eliteshop.colombia.shared.notification.domain.model;

import java.time.Instant;

public record SlackMessage(
        SlackMessageId id,
        NotificationChannel channel,
        String text,
        String status,
        int retryCount,
        int maxRetries,
        Instant createdAt,
        Instant nextRetryAt
) {

    public static SlackMessage create(String text) {
        return new SlackMessage(
                SlackMessageId.generate(),
                NotificationChannel.SLACK,
                text,
                "PENDING",
                0,
                5,
                Instant.now(),
                Instant.now());
    }

    public SlackMessage withStatus(String status) {
        return new SlackMessage(
                id, channel, text, status, retryCount, maxRetries, createdAt, nextRetryAt);
    }

    public SlackMessage withIncrementedRetry(Instant nextRetryAt) {
        return new SlackMessage(
                id, channel, text, status, retryCount + 1, maxRetries, createdAt, nextRetryAt);
    }

    public boolean canRetry() {
        return retryCount < maxRetries;
    }
}