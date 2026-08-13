package com.eliteshop.colombia.shared.notification.domain.port;

import com.eliteshop.colombia.shared.notification.domain.model.SlackMessage;
import com.eliteshop.colombia.shared.notification.domain.model.SlackMessageId;
import java.util.List;

public interface SlackMessageRepository {

    SlackMessage save(SlackMessage message);

    void updateStatus(SlackMessageId id, String status);

    void incrementRetryCount(SlackMessageId id, String nextRetryAt);

    List<SlackMessage> findByStatus(String status);
}