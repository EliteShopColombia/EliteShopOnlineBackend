package com.eliteshop.colombia.payment.application.usecase;

import com.eliteshop.colombia.payment.domain.exception.PaymentAlreadyProcessedException;
import com.eliteshop.colombia.payment.domain.model.*;
import com.eliteshop.colombia.payment.domain.port.*;
import com.eliteshop.colombia.payment.infrastructure.config.EpaycoProperties;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class CreateCheckoutSessionUseCase {

  private final PaymentGateway paymentGateway;
  private final PaymentRepository paymentRepository;
  private final EpaycoProperties epaycoProperties;

  public Mono<CheckoutSession> execute(CheckoutSessionRequest request) {

    return Mono.justOrEmpty(paymentRepository.findByInvoice(request.getInvoice()))
        .flatMap(
            existingPayment -> {
              if (existingPayment.getStatus() == PaymentStatus.APPROVED) {
                return Mono.error(
                    new PaymentAlreadyProcessedException(
                        "El pago con invoice " + request.getInvoice() + " ya fue aprobado"));
              }
              if (existingPayment.getStatus() == PaymentStatus.PENDING
                  && existingPayment.getCreatedAt() != null
                  && existingPayment.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(30))) {
                log.info("Reutilizando sesión existente para invoice: {}", request.getInvoice());
                return createGatewaySession(request, existingPayment);
              }
              return createNewPayment(request);
            })
        .switchIfEmpty(Mono.defer(() -> createNewPayment(request)));
  }

  private Mono<CheckoutSession> createGatewaySession(
      CheckoutSessionRequest request, Payment existingPayment) {
    return paymentGateway
        .createSession(
            CheckoutSessionRequest.builder()
                .orderId(request.getOrderId())
                .storeName("EliteShop Colombia")
                .currency("COP")
                .amount(request.getAmount())
                .invoice(request.getInvoice())
                .description("Pago de pedido")
                .customerEmail(request.getCustomerEmail())
                .billing(request.getBilling())
                .responseUrl(epaycoProperties.getResponseUrl())
                .build())
        .map(
            session -> {
              existingPayment.setSessionId(session.getSessionId());
              paymentRepository.save(existingPayment);
              return CheckoutSession.builder()
                  .sessionId(session.getSessionId())
                  .token(session.getToken())
                  .invoice(existingPayment.getInvoice())
                  .build();
            });
  }

  private Mono<CheckoutSession> createNewPayment(CheckoutSessionRequest request) {

    Payment payment =
        Payment.builder()
            .id(Payment.generateId())
            .orderId(request.getOrderId())
            .amount(request.getAmount())
            .currency("COP")
            .method(resolvePaymentMethod(request.getPaymentMethod()))
            .status(PaymentStatus.PENDING)
            .invoice(request.getInvoice())
            .customerEmail(request.getCustomerEmail())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    Payment savedPayment = paymentRepository.save(payment);
    log.info(
        "Pago PENDING creado: id={}, invoice={}", savedPayment.getId(), savedPayment.getInvoice());

    return paymentGateway
        .createSession(
            CheckoutSessionRequest.builder()
                .orderId(request.getOrderId())
                .storeName("EliteShop Colombia")
                .currency("COP")
                .amount(request.getAmount())
                .invoice(request.getInvoice())
                .description("Pago de pedido")
                .customerEmail(request.getCustomerEmail())
                .billing(request.getBilling())
                .responseUrl(epaycoProperties.getResponseUrl())
                .build())
        .map(
            session -> {
              savedPayment.setSessionId(session.getSessionId());
              paymentRepository.save(savedPayment);
              log.info(
                  "Session creada: sessionId={}, invoice={}",
                  session.getSessionId(),
                  savedPayment.getInvoice());

              return CheckoutSession.builder()
                  .sessionId(session.getSessionId())
                  .token(session.getToken())
                  .invoice(savedPayment.getInvoice())
                  .build();
            })
        .onErrorResume(
            error -> {
              log.error("Error creando session de pago: {}", error.getMessage());
              savedPayment.setStatus(PaymentStatus.ERROR);
              paymentRepository.save(savedPayment);
              return Mono.error(error);
            });
  }

  private PaymentMethod resolvePaymentMethod(String method) {
    if (method == null || method.isBlank()) {
      return PaymentMethod.CARD;
    }
    try {
      return PaymentMethod.valueOf(method.toUpperCase());
    } catch (IllegalArgumentException e) {
      log.warn("Metodo de pago no valido: {}, usando CARD por defecto", method);
      return PaymentMethod.CARD;
    }
  }
}
