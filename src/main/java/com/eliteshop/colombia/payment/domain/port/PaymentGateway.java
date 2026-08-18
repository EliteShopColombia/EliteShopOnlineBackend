package com.eliteshop.colombia.payment.domain.port;

import com.eliteshop.colombia.payment.domain.model.*;
import reactor.core.publisher.Mono;

public interface PaymentGateway {
  Mono<String> login();

  Mono<CheckoutSession> createSession(CheckoutSessionRequest request);

  Mono<Payment> confirmTransaction(String refId);
}
