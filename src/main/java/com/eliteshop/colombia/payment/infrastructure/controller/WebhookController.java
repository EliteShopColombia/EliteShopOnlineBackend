package com.eliteshop.colombia.payment.infrastructure.controller;

import com.eliteshop.colombia.payment.application.usecase.ConfirmPaymentUseCase;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/webhooks/epayco")
@RequiredArgsConstructor
public class WebhookController {

  private final ConfirmPaymentUseCase confirmPaymentUseCase;

  @PostMapping
  public ResponseEntity<Void> handleWebhook(@RequestBody Map<String, Object> payload) {

    log.info("Webhook ePayco recibido");

    String refId = (String) payload.get("x_ref_payco");
    String status = (String) payload.get("x_respuesta");

    if (refId != null) {
      log.info("Procesando pago refId={}, status={}", refId, status);

      confirmPaymentUseCase
          .execute(refId)
          .subscribe(
              payment -> log.info("Pago actualizado: {}", payment),
              error -> log.error("Error actualizando pago", error));
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
