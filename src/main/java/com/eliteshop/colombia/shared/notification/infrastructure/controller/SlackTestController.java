package com.eliteshop.colombia.shared.notification.infrastructure.controller;

import com.eliteshop.colombia.shared.notification.infrastructure.adapter.SlackWebhookAdapter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Profile("dev")
public class SlackTestController {

  private final SlackWebhookAdapter slackWebhookAdapter;

  @GetMapping("/test-slack")
  public String testSlack(@RequestParam(defaultValue = "health") String channel) {
    String timestamp =
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    String message =
        String.format(
            ":white_check_mark: *Test de notificación Slack*\n"
                + "EliteShop Backend está funcionando correctamente\n"
                + "Canal: %s\n"
                + "Hora: %s",
            channel, timestamp);

    slackWebhookAdapter.sendToChannel(channel, message);
    return "Notificación enviada a canal: " + channel;
  }
}
