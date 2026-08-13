package com.eliteshop.colombia.shared.notification.domain.port;

import com.eliteshop.colombia.shared.notification.domain.model.SlackMessage;

public interface NotificationPort {
    void send(SlackMessage message);
}