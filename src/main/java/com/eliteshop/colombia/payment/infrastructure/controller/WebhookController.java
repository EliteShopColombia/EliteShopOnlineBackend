package com.eliteshop.colombia.payment.infrastructure.controller;

import com.eliteshop.colombia.payment.application.usecase.ConfirmPaymentUseCase;
import com.eliteshop.colombia.payment.infrastructure.config.GatewayProperties;
import com.eliteshop.colombia.shared.notification.infrastructure.controller.WebhookSignatureValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/webhooks/epayco")
@RequiredArgsConstructor
public class WebhookController {

  private final ConfirmPaymentUseCase confirmPaymentUseCase;
  private final GatewayProperties gatewayProperties;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @PostMapping
  public ResponseEntity<Void> handleWebhook(
      @RequestBody byte[] rawBody,
      @RequestHeader(value = "X-Signature", required = false) String signature) {

    log.info("Webhook ePayco recibido");

    if (gatewayProperties.getSecret() == null || gatewayProperties.getSecret().isBlank()) {
      log.error("Gateway secret no configurado, rechazando webhook");
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    String payloadJson = new String(rawBody);
    if (!WebhookSignatureValidator.isValid(payloadJson, signature, gatewayProperties.getSecret())) {
      log.warn("Firma de webhook invalida, rechazando request");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    try {
      JsonNode payload = objectMapper.readTree(rawBody);
      String refId = payload.path("x_ref_payco").asText(null);
      String status = payload.path("x_respuesta").asText(null);

      if (refId != null) {
        log.info("Procesando pago refId={}, status={}", refId, status);
        confirmPaymentUseCase.execute(refId).block();
      }
    } catch (Exception e) {
      log.error("Error procesando webhook ePayco", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    return ResponseEntity.ok().build();
  }

  @GetMapping
  public ResponseEntity<Map<String, String>> healthCheck() {
    return ResponseEntity.ok(
        Map.of(
            "status", "ok",
            "service", "epayco-webhook"));
  }
}
