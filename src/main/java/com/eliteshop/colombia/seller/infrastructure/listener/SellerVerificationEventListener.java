package com.eliteshop.colombia.seller.infrastructure.listener;

import com.eliteshop.colombia.seller.domain.event.SellerVerificationCompletedEvent;
import com.eliteshop.colombia.seller.infrastructure.email.SellerVerificationEmailService;
import com.eliteshop.colombia.seller.infrastructure.persistence.SellerJpaRepository;
import com.eliteshop.colombia.shared.notification.infrastructure.adapter.SlackWebhookAdapter;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SellerVerificationEventListener {

  private final SellerJpaRepository sellerRepository;
  private final SlackWebhookAdapter slackWebhookAdapter;
  private final SellerVerificationEmailService emailService;

  private static final DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

  @Async
  @EventListener
  public void handleVerificationCompleted(SellerVerificationCompletedEvent event) {
    log.info(
        "Evento de verificacion recibido: sellerId={}, verified={}",
        event.sellerId(),
        event.verified());

    updateSellerVerifiedStatus(event.sellerId(), event.verified());

    if (event.verified()) {
      sendApprovedNotification(event);
      emailService.sendApprovedEmail(event);
    } else {
      sendRejectedNotification(event);
      emailService.sendRejectedEmail(event);
    }
  }

  private void updateSellerVerifiedStatus(UUID sellerId, boolean verified) {
    sellerRepository
        .findById(sellerId)
        .ifPresent(
            seller -> {
              seller.setIsVerified(verified);
              seller.setUpdatedAt(Timestamp.from(Instant.now()));
              sellerRepository.save(seller);
              log.info(
                  "Estado de verificacion actualizado: sellerId={}, isVerified={}",
                  sellerId,
                  verified);
            });
  }

  private void sendApprovedNotification(SellerVerificationCompletedEvent event) {
    String message =
        String.format(
            ":white_check_mark: *Vendedor Verificado*\n"
                + "• ID: `%s`\n"
                + "• Confianza: %.0f%%\n"
                + "• Hora: %s",
            event.sellerId(),
            event.confidence() * 100,
            TIME_FORMATTER.format(event.occurredAt()));
    slackWebhookAdapter.sendToChannel("notifications-test", message);
  }

  private void sendRejectedNotification(SellerVerificationCompletedEvent event) {
    String message =
        String.format(
            ":rotating_light: *Intento de Fraude Detectado*\n"
                + "• ID Vendedor: `%s`\n"
                + "• Razon: %s\n"
                + "• Hora: %s",
            event.sellerId(),
            event.message(),
            TIME_FORMATTER.format(event.occurredAt()));
    slackWebhookAdapter.sendToChannel("notifications-test", message);
  }
}
