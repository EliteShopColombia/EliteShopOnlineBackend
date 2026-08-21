package com.eliteshop.colombia.payment.infrastructure.controller;

import com.eliteshop.colombia.payment.application.usecase.*;
import com.eliteshop.colombia.payment.domain.model.*;
import com.eliteshop.colombia.payment.infrastructure.dto.*;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

  private final CreateCheckoutSessionUseCase createCheckoutSessionUseCase;
  private final ConfirmPaymentUseCase confirmPaymentUseCase;
  private final RetryPaymentUseCase retryPaymentUseCase;
  private final RetryWithSavedCardUseCase retryWithSavedCardUseCase;

  @PostMapping("/checkout-session")
  public ResponseEntity<CreateCheckoutSessionResponse> createCheckoutSession(
      @Valid @RequestBody CreateCheckoutSessionRequest request) {

    log.info("Creando sesion de checkout para orderId={}", request.getOrderId());

    String invoice = "INV-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();

    CheckoutSessionRequest sessionRequest =
        CheckoutSessionRequest.builder()
            .orderId(request.getOrderId())
            .storeName("EliteShop Colombia")
            .amount(request.getAmount())
            .currency("COP")
            .invoice(invoice)
            .description("Pago de pedido")
            .customerEmail(request.getCustomerEmail())
            .paymentMethod(request.getPaymentMethod())
            .billing(request.getBilling())
            .build();

    return createCheckoutSessionUseCase
        .execute(sessionRequest)
        .map(
            session ->
                ResponseEntity.ok(
                    CreateCheckoutSessionResponse.builder()
                        .sessionId(session.getSessionId())
                        .token(session.getToken())
                        .invoice(session.getInvoice())
                        .build()))
        .block();
  }

  @PostMapping("/confirm/{refId}")
  public ResponseEntity<ConfirmPaymentResponse> confirmPayment(@PathVariable String refId) {

    log.info("Confirmando pago refId={}", refId);

    return confirmPaymentUseCase
        .execute(refId)
        .map(
            payment ->
                ResponseEntity.ok(
                    ConfirmPaymentResponse.builder()
                        .status(payment.getStatus().name())
                        .refId(payment.getEpaycoRefId())
                        .invoice(payment.getInvoice())
                        .build()))
        .blockOptional()
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/{invoice}")
  public ResponseEntity<Map<String, String>> getPaymentByInvoice(@PathVariable String invoice) {

    log.info("Consultando pago por invoice={}", invoice);

    return confirmPaymentUseCase
        .getPaymentByInvoice(invoice)
        .map(
            payment ->
                ResponseEntity.ok(
                    Map.of(
                        "status", payment.getStatus().name(),
                        "invoice", payment.getInvoice() != null ? payment.getInvoice() : "",
                        "orderId",
                            payment.getOrderId() != null ? payment.getOrderId().toString() : "")))
        .blockOptional()
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping("/orders/{orderId}/retry")
  public ResponseEntity<?> retryPayment(
      @PathVariable UUID orderId, @RequestBody(required = false) RetryPaymentRequest request) {

    log.info(
        "Reintentando pago para orderId={}, paymentMethodId={}",
        orderId,
        request != null ? request.getPaymentMethodId() : "Smart Checkout");

    if (request != null && request.getPaymentMethodId() != null) {
      return retryWithSavedCardUseCase
          .execute(orderId, request.getPaymentMethodId(), request.getCvv())
          .map(
              payment ->
                  ResponseEntity.ok(
                      Map.of(
                          "status", payment.getStatus().name(),
                          "invoice", payment.getInvoice() != null ? payment.getInvoice() : "",
                          "refId",
                              payment.getEpaycoRefId() != null ? payment.getEpaycoRefId() : "")))
          .blockOptional()
          .orElse(ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST).build());
    }

    return retryPaymentUseCase
        .execute(orderId)
        .map(
            session ->
                ResponseEntity.ok(
                    CreateCheckoutSessionResponse.builder()
                        .sessionId(session.getSessionId())
                        .token(session.getToken())
                        .invoice(session.getInvoice())
                        .build()))
        .blockOptional()
        .orElse(ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST).build());
  }
}
