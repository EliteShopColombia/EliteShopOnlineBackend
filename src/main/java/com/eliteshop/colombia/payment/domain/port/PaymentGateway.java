package com.eliteshop.colombia.payment.domain.port;

import com.eliteshop.colombia.payment.domain.model.*;
import com.eliteshop.colombia.payment.domain.model.paymentmethod.TokenizedCard;
import java.math.BigDecimal;
import reactor.core.publisher.Mono;

public interface PaymentGateway {
  Mono<String> login();

  Mono<CheckoutSession> createSession(CheckoutSessionRequest request);

  Mono<Payment> confirmTransaction(String refId);

  Mono<TokenizedCard> tokenizeCard(String cardNumber, String cvc, int expiryMonth, int expiryYear);

  Mono<String> createEpaycoCustomer(
      String token,
      String name,
      String lastName,
      String email,
      String phone,
      String docType,
      String docNumber);

  Mono<Void> addTokenToCustomer(String customerId, String token);

  Mono<Payment> chargeWithToken(
      String token,
      String customerId,
      String cvc,
      BigDecimal amount,
      String invoice,
      String name,
      String lastName,
      String email,
      String docType,
      String docNumber);
}
