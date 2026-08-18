package com.eliteshop.colombia.payment.application.usecase;

import com.eliteshop.colombia.payment.domain.exception.PaymentNotFoundException;
import com.eliteshop.colombia.payment.domain.model.*;
import com.eliteshop.colombia.payment.domain.port.*;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class ConfirmPaymentUseCase {

  private final PaymentGateway paymentGateway;
  private final PaymentRepository paymentRepository;

  public Mono<Payment> execute(String refId) {

    return paymentGateway
        .confirmTransaction(refId)
        .flatMap(
            epaycoPayment -> {
              return Mono.justOrEmpty(paymentRepository.findByEpaycoRefId(refId))
                  .switchIfEmpty(
                      Mono.error(
                          new PaymentNotFoundException("Pago no encontrado para refId: " + refId)))
                  .flatMap(
                      payment -> {
                        if (payment.getStatus() == PaymentStatus.APPROVED) {
                          log.info(
                              "Pago ya aprobado, ignorando actualizacion para refId: {}", refId);
                          return Mono.just(payment);
                        }

                        payment.setStatus(epaycoPayment.getStatus());
                        payment.setEpaycoRefId(refId);
                        payment.setUpdatedAt(LocalDateTime.now());

                        Payment savedPayment = paymentRepository.save(payment);

                        log.info(
                            "Pago actualizado: refId={}, status={}",
                            refId,
                            savedPayment.getStatus());

                        return Mono.just(savedPayment);
                      });
            })
        .onErrorResume(
            e -> {
              if (e instanceof PaymentNotFoundException) {
                return Mono.error(e);
              }
              log.error("Error confirmando pago refId={}: {}", refId, e.getMessage());
              return Mono.error(e);
            });
  }

  public Mono<Payment> getPaymentByInvoice(String invoice) {
    return Mono.justOrEmpty(paymentRepository.findByInvoice(invoice))
        .switchIfEmpty(
            Mono.error(
                new PaymentNotFoundException("Pago no encontrado para invoice: " + invoice)));
  }
}
