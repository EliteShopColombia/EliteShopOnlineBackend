package com.eliteshop.colombia.payment.application.usecase;

import com.eliteshop.colombia.order.domain.model.OrderStatus;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import com.eliteshop.colombia.payment.domain.exception.PaymentAlreadyProcessedException;
import com.eliteshop.colombia.payment.domain.model.*;
import com.eliteshop.colombia.payment.domain.port.PaymentGateway;
import com.eliteshop.colombia.payment.domain.port.PaymentRepository;
import com.eliteshop.colombia.payment.infrastructure.config.EpaycoProperties;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class RetryPaymentUseCase {

  private final PaymentGateway paymentGateway;
  private final PaymentRepository paymentRepository;
  private final OrderRepository orderRepository;
  private final EpaycoProperties epaycoProperties;

  public Mono<CheckoutSession> execute(UUID orderId) {

    log.info("Reintentando pago para orderId={}", orderId);

    return Mono.justOrEmpty(
            orderRepository.findById(
                new com.eliteshop.colombia.order.domain.model.OrderId(orderId)))
        .switchIfEmpty(
            Mono.error(new IllegalArgumentException("Orden no encontrada con id: " + orderId)))
        .flatMap(
            order -> {
              if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
                return Mono.error(
                    new PaymentAlreadyProcessedException(
                        "Solo se pueden reintentar pagos de ordenes con estado PENDING_PAYMENT. Estado actual: "
                            + order.getStatus()));
              }

              return Mono.justOrEmpty(paymentRepository.findByOrderId(orderId))
                  .switchIfEmpty(
                      Mono.error(
                          new com.eliteshop.colombia.payment.domain.exception
                              .PaymentNotFoundException(
                              "No existe pago asociado a la orden: " + orderId)))
                  .flatMap(
                      existingPayment -> {
                        String newInvoice =
                            "INV-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
                        log.info(
                            "Generando nueva invoice para reintento: {} -> {}",
                            existingPayment.getInvoice(),
                            newInvoice);

                        existingPayment.setInvoice(newInvoice);
                        existingPayment.setStatus(PaymentStatus.PENDING);
                        existingPayment.setSessionId(null);
                        existingPayment.setEpaycoRefId(null);
                        existingPayment.setUpdatedAt(LocalDateTime.now());
                        paymentRepository.save(existingPayment);

                        return paymentGateway
                            .createSession(
                                CheckoutSessionRequest.builder()
                                    .orderId(orderId)
                                    .storeName("EliteShop Colombia")
                                    .amount(existingPayment.getAmount())
                                    .currency("COP")
                                    .invoice(newInvoice)
                                    .description("Reintento de pago de pedido")
                                    .customerEmail(existingPayment.getCustomerEmail())
                                    .paymentMethod(
                                        existingPayment.getMethod() != null
                                            ? existingPayment.getMethod().name()
                                            : "CARD")
                                    .responseUrl(epaycoProperties.getResponseUrl())
                                    .build())
                            .map(
                                session -> {
                                  existingPayment.setSessionId(session.getSessionId());
                                  paymentRepository.save(existingPayment);
                                  log.info(
                                      "Sesion de reintento creada: sessionId={}, invoice={}",
                                      session.getSessionId(),
                                      newInvoice);
                                  return CheckoutSession.builder()
                                      .sessionId(session.getSessionId())
                                      .token(session.getToken())
                                      .invoice(newInvoice)
                                      .build();
                                })
                            .onErrorResume(
                                error -> {
                                  log.error(
                                      "Error creando sesión de reintento: {}", error.getMessage());
                                  existingPayment.setStatus(PaymentStatus.ERROR);
                                  existingPayment.setUpdatedAt(LocalDateTime.now());
                                  paymentRepository.save(existingPayment);
                                  return Mono.error(error);
                                });
                      });
            });
  }
}
