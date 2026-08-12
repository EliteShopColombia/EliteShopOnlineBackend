package com.eliteshop.colombia.seller.infrastructure.email;

import com.eliteshop.colombia.seller.domain.event.SellerVerificationCompletedEvent;
import com.eliteshop.colombia.seller.infrastructure.persistence.SellerEntity;
import com.eliteshop.colombia.seller.infrastructure.persistence.SellerJpaRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerVerificationEmailService {

  private final JavaMailSender mailSender;
  private final TemplateEngine templateEngine;
  private final SellerJpaRepository sellerRepository;

  @Value("${spring.mail.username:noreply@eliteshop.com}")
  private String fromEmail;

  @Async
  public void sendApprovedEmail(SellerVerificationCompletedEvent event) {
    Optional<SellerEntity> sellerOpt = sellerRepository.findById(event.sellerId());
    if (sellerOpt.isEmpty()) {
      log.warn("Vendedor no encontrado para enviar email: {}", event.sellerId());
      return;
    }

    SellerEntity seller = sellerOpt.get();
    String toEmail = seller.getContact() != null ? seller.getContact().getEmail() : null;
    if (toEmail == null) {
      log.warn("Vendedor no tiene email configurado: {}", event.sellerId());
      return;
    }

    try {
      Context context = new Context();
      context.setVariable("sellerName", seller.getFullname());

      String htmlContent = templateEngine.process("email/verification-approved", context);

      var message = mailSender.createMimeMessage();
      var helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setFrom(fromEmail);
      helper.setTo(toEmail);
      helper.setSubject("EliteShop Colombia - Identidad Verificada");
      helper.setText(htmlContent, true);

      mailSender.send(message);
      log.info("Email de verificacion aprobada enviado a: {}", toEmail);
    } catch (Exception e) {
      log.error("Error enviando email de aprobacion: {}", e.getMessage());
    }
  }

  @Async
  public void sendRejectedEmail(SellerVerificationCompletedEvent event) {
    Optional<SellerEntity> sellerOpt = sellerRepository.findById(event.sellerId());
    if (sellerOpt.isEmpty()) {
      log.warn("Vendedor no encontrado para enviar email: {}", event.sellerId());
      return;
    }

    SellerEntity seller = sellerOpt.get();
    String toEmail = seller.getContact() != null ? seller.getContact().getEmail() : null;
    if (toEmail == null) {
      log.warn("Vendedor no tiene email configurado: {}", event.sellerId());
      return;
    }

    try {
      Context context = new Context();
      context.setVariable("sellerName", seller.getFullname());
      context.setVariable("reason", event.message());

      String htmlContent = templateEngine.process("email/verification-rejected", context);

      var message = mailSender.createMimeMessage();
      var helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setFrom(fromEmail);
      helper.setTo(toEmail);
      helper.setSubject("EliteShop Colombia - Verificacion Rechazada");
      helper.setText(htmlContent, true);

      mailSender.send(message);
      log.info("Email de verificacion rechazada enviado a: {}", toEmail);
    } catch (Exception e) {
      log.error("Error enviando email de rechazo: {}", e.getMessage());
    }
  }
}
