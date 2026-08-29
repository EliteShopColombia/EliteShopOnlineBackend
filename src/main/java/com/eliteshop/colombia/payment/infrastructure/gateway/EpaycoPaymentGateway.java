package com.eliteshop.colombia.payment.infrastructure.gateway;

import com.eliteshop.colombia.payment.domain.exception.PaymentGatewayException;
import com.eliteshop.colombia.payment.domain.model.*;
import com.eliteshop.colombia.payment.domain.model.paymentmethod.TokenizedCard;
import com.eliteshop.colombia.payment.domain.port.PaymentGateway;
import com.eliteshop.colombia.payment.infrastructure.config.EpaycoProperties;
import com.eliteshop.colombia.payment.infrastructure.dto.*;
import java.math.BigDecimal;
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
  private final WebClient classicWebClient;
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

  private Mono<String> loginClassic() {

    Map<String, Object> body = new HashMap<>();
    body.put("public_key", properties.getPublicKey());
    body.put("private_key", properties.getPrivateKey());

    return classicWebClient
        .post()
        .uri("/v1/auth/login")
        .bodyValue(body)
        .retrieve()
        .bodyToMono(ClassicLoginResponse.class)
        .map(ClassicLoginResponse::getBearerToken)
        .doOnNext(token -> log.info("Login ePayco API clásica exitoso"))
        .doOnError(error -> log.error("Error en login ePayco API clásica: {}", error.getMessage()));
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

              if (request.getResponseUrl() != null) {
                body.put("response", request.getResponseUrl());
              }

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
                      response -> {
                        if (response.getData() == null) {
                          log.error(
                              "ePayco session create returned null data. success={}, body={}",
                              response.isSuccess(),
                              response);
                          throw new PaymentGatewayException(
                              "ePayco no retornó datos de sesión de pago");
                        }
                        String sessionId = response.getData().getSessionId();
                        String sessionToken = response.getData().getToken();
                        if (sessionId == null || sessionToken == null) {
                          log.error(
                              "ePayco session create returned null sessionId/token. "
                                  + "sessionId={}, token={}, success={}",
                              sessionId,
                              sessionToken,
                              response.isSuccess());
                          throw new PaymentGatewayException(
                              "ePayco no retornó sessionId o token válido");
                        }
                        return CheckoutSession.builder()
                            .sessionId(sessionId)
                            .token(sessionToken)
                            .build();
                      });
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

  @Override
  public Mono<TokenizedCard> tokenizeCard(
      String cardNumber, String cvc, int expiryMonth, int expiryYear) {

    return loginClassic()
        .flatMap(
            token -> {
              Map<String, Object> body = new HashMap<>();
              body.put("card[number]", cardNumber);
              body.put("card[cvc]", cvc);
              body.put("card[exp_month]", String.valueOf(expiryMonth));
              body.put("card[exp_year]", String.valueOf(expiryYear));

              return classicWebClient
                  .post()
                  .uri("/v1/tokens")
                  .header("Authorization", "Bearer " + token)
                  .bodyValue(body)
                  .retrieve()
                  .bodyToMono(TokenizeResponse.class)
                  .map(
                      response -> {
                        String last4 = response.getLast4();
                        if (last4 == null || last4.isBlank()) {
                          throw new com.eliteshop.colombia.payment.domain.exception
                              .PaymentGatewayException("No se pudo obtener el last4 de la tarjeta");
                        }
                        return new TokenizedCard(
                            response.getId(),
                            last4,
                            response.getCard().getName(),
                            Integer.parseInt(response.getCard().getExpMonth()),
                            Integer.parseInt(response.getCard().getExpYear()));
                      });
            });
  }

  @Override
  public Mono<String> createEpaycoCustomer(
      String token,
      String name,
      String lastName,
      String email,
      String phone,
      String docType,
      String docNumber) {

    return loginClassic()
        .flatMap(
            authToken -> {
              Map<String, Object> body = new HashMap<>();
              body.put("token_card", token);
              body.put("name", name);
              body.put("last_name", lastName);
              body.put("email", email);
              body.put("phone", phone);
              body.put("cell_phone", phone);
              body.put("default", true);
              if (docType != null && !docType.isBlank()) {
                body.put("doc_type", docType);
              }
              if (docNumber != null && !docNumber.isBlank()) {
                body.put("doc_number", docNumber);
              }

              return classicWebClient
                  .post()
                  .uri("/payment/v1/customer/create")
                  .header("Authorization", "Bearer " + authToken)
                  .bodyValue(body)
                  .retrieve()
                  .bodyToMono(CreateCustomerResponse.class)
                  .map(
                      response -> {
                        if (response.getData() == null
                            || response.getData().getCustomerId() == null) {
                          throw new com.eliteshop.colombia.payment.domain.exception
                              .PaymentGatewayException("No se pudo crear el customer en ePayco");
                        }
                        return response.getData().getCustomerId();
                      });
            });
  }

  @Override
  public Mono<Void> addTokenToCustomer(String customerId, String token) {

    return loginClassic()
        .flatMap(
            authToken -> {
              Map<String, Object> body = new HashMap<>();
              body.put("customer_id", customerId);
              body.put("token_card", token);

              return classicWebClient
                  .post()
                  .uri("/v1/customer/add/token")
                  .header("Authorization", "Bearer " + authToken)
                  .bodyValue(body)
                  .retrieve()
                  .bodyToMono(Void.class);
            });
  }

  @Override
  public Mono<Payment> chargeWithToken(
      String token,
      String customerId,
      String cvc,
      BigDecimal amount,
      String invoice,
      String name,
      String lastName,
      String email,
      String docType,
      String docNumber) {

    return loginClassic()
        .flatMap(
            authToken -> {
              Map<String, Object> body = new HashMap<>();
              body.put("token_card", token);
              body.put("customer_id", customerId);
              body.put("cvc", cvc);
              body.put("value", amount);
              body.put("invoice", invoice);
              body.put("bill", invoice);
              body.put("description", "Compra EliteShop Colombia - " + invoice);
              body.put("currency", "COP");
              body.put("dues", 1);
              body.put("name", name);
              body.put("last_name", lastName);
              body.put("email", email);
              body.put("doc_type", docType);
              body.put("doc_number", docNumber);

              return classicWebClient
                  .post()
                  .uri("/payment/v1/charge/create")
                  .header("Authorization", "Bearer " + authToken)
                  .bodyValue(body)
                  .retrieve()
                  .bodyToMono(ChargeResponse.class)
                  .map(
                      response -> {
                        PaymentStatus status;
                        if (!response.isSuccess() || response.getData() == null) {
                          status = PaymentStatus.ERROR;
                        } else {
                          status = mapStatus(response.getData().getEstado());
                        }
                        return Payment.builder()
                            .epaycoRefId(
                                response.getData() != null ? response.getData().getRefId() : null)
                            .status(status)
                            .build();
                      });
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
