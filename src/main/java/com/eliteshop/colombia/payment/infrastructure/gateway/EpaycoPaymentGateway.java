package com.eliteshop.colombia.payment.infrastructure.gateway;

import com.eliteshop.colombia.payment.domain.model.*;
import com.eliteshop.colombia.payment.domain.port.PaymentGateway;
import com.eliteshop.colombia.payment.infrastructure.config.EpaycoProperties;
import com.eliteshop.colombia.payment.infrastructure.dto.*;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class EpaycoPaymentGateway implements PaymentGateway {

  private final WebClient apifyWebClient;
  private final EpaycoProperties properties;

  @Override
  public Mono<String> login() {

    return apifyWebClient
        .post()
        .uri("/login")
        .retrieve()
        .bodyToMono(LoginResponse.class)
        .map(LoginResponse::getToken)
        .doOnNext(token -> log.info("Login ePayco exitoso"))
        .doOnError(error -> log.error("Error en login ePayco: {}", error.getMessage()));
  }

  @Override
  public Mono<CheckoutSession> createSession(CheckoutSessionRequest request) {

    return login()
        .flatMap(
            token -> {
              Map<String, Object> body = new HashMap<>();
              body.put("checkout_version", "2");
              body.put("name", request.getStoreName());
              body.put("currency", request.getCurrency());
              body.put("amount", request.getAmount());
              body.put("invoice", request.getInvoice());
              body.put("description", request.getDescription());
              body.put("country", "CO");
              body.put("lang", "ES");

              if (request.getBilling() != null) {
                body.put("billing", request.getBilling());
              }

              return apifyWebClient
                  .post()
                  .uri("/payment/session/create")
                  .header("Authorization", "Bearer " + token)
                  .bodyValue(body)
                  .retrieve()
                  .bodyToMono(CreateSessionResponse.class)
                  .map(
                      response ->
                          CheckoutSession.builder()
                              .sessionId(response.getData().getSessionId())
                              .token(response.getData().getToken())
                              .build());
            });
  }

  @Override
  public Mono<Payment> confirmTransaction(String refId) {

    return login()
        .flatMap(
            token -> {
              return apifyWebClient
                  .get()
                  .uri("/transaction/{refId}", refId)
                  .header("Authorization", "Bearer " + token)
                  .retrieve()
                  .bodyToMono(TransactionResponse.class)
                  .map(
                      response ->
                          Payment.builder()
                              .epaycoRefId(response.getRefId())
                              .status(mapStatus(response.getStatus()))
                              .build());
            });
  }

  private PaymentStatus mapStatus(String status) {
    if (status == null) return PaymentStatus.PENDING;

    return switch (status.toUpperCase()) {
      case "APPROVED", "ACEPTADA" -> PaymentStatus.APPROVED;
      case "DECLINED", "RECHAZADA" -> PaymentStatus.DECLINED;
      case "PENDING", "PENDIENTE" -> PaymentStatus.PENDING;
      default -> PaymentStatus.ERROR;
    };
  }
}
