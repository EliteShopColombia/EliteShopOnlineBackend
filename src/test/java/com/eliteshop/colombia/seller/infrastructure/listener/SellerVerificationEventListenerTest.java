package com.eliteshop.colombia.seller.infrastructure.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.eliteshop.colombia.seller.domain.event.SellerVerificationCompletedEvent;
import com.eliteshop.colombia.seller.infrastructure.email.SellerVerificationEmailService;
import com.eliteshop.colombia.seller.infrastructure.persistence.SellerEntity;
import com.eliteshop.colombia.seller.infrastructure.persistence.SellerJpaRepository;
import com.eliteshop.colombia.shared.notification.infrastructure.adapter.SlackWebhookAdapter;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Verifica el eslabón crítico del flujo: cuando la verificación se completa, el vendedor queda
 * habilitado (o no) y se envía la notificación correspondiente.
 */
@ExtendWith(MockitoExtension.class)
class SellerVerificationEventListenerTest {

  @Mock private SellerJpaRepository sellerRepository;
  @Mock private SlackWebhookAdapter slackWebhookAdapter;
  @Mock private SellerVerificationEmailService emailService;

  @InjectMocks private SellerVerificationEventListener listener;

  private SellerEntity buildSeller(UUID sellerId) {
    SellerEntity seller = new SellerEntity();
    seller.setId(sellerId);
    seller.setTradeName("Tienda Prueba");
    seller.setIsVerified(false);
    return seller;
  }

  @Test
  void shouldEnableSellerAndSendApprovedEmailWhenApproved() {
    UUID sellerId = UUID.randomUUID();
    SellerEntity seller = buildSeller(sellerId);
    when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));

    listener.handleVerificationCompleted(
        SellerVerificationCompletedEvent.approved(sellerId, 0.93));

    assertThat(seller.getIsVerified()).isTrue();
    verify(sellerRepository).save(seller);
    verify(emailService).sendApprovedEmail(any(SellerVerificationCompletedEvent.class));
    verify(emailService, never()).sendRejectedEmail(any(SellerVerificationCompletedEvent.class));
  }

  @Test
  void shouldKeepSellerDisabledAndSendRejectedEmailWhenRejected() {
    UUID sellerId = UUID.randomUUID();
    SellerEntity seller = buildSeller(sellerId);
    when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));

    listener.handleVerificationCompleted(
        SellerVerificationCompletedEvent.rejected(sellerId, "Las caras no coinciden"));

    assertThat(seller.getIsVerified()).isFalse();
    verify(sellerRepository).save(seller);
    verify(emailService).sendRejectedEmail(any(SellerVerificationCompletedEvent.class));
    verify(emailService, never()).sendApprovedEmail(any(SellerVerificationCompletedEvent.class));
  }

  @Test
  void shouldNotFailWhenSellerDoesNotExist() {
    UUID sellerId = UUID.randomUUID();
    when(sellerRepository.findById(sellerId)).thenReturn(Optional.empty());

    listener.handleVerificationCompleted(
        SellerVerificationCompletedEvent.rejected(sellerId, "sin vendedor"));

    verify(sellerRepository, never()).save(any());
  }
}
