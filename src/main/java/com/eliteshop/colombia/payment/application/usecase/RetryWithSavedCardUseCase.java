package com.eliteshop.colombia.payment.application.usecase;

import com.eliteshop.colombia.checkout.domain.exception.CvvRequiredException;
import com.eliteshop.colombia.customer.domain.model.Customer;
import com.eliteshop.colombia.customer.domain.repository.CustomerRepository;
import com.eliteshop.colombia.order.application.OrderUpdateUseCase;
import com.eliteshop.colombia.order.domain.model.*;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import com.eliteshop.colombia.payment.domain.exception.PaymentAlreadyProcessedException;
import com.eliteshop.colombia.payment.domain.model.*;
import com.eliteshop.colombia.payment.domain.port.CustomerPaymentMethodRepository;
import com.eliteshop.colombia.payment.domain.port.PaymentGateway;
import com.eliteshop.colombia.payment.domain.port.PaymentRepository;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class RetryWithSavedCardUseCase {

  private final PaymentGateway paymentGateway;
  private final PaymentRepository paymentRepository;
  private final OrderRepository orderRepository;
  private final CustomerPaymentMethodRepository paymentMethodRepository;
  private final CustomerRepository customerRepository;
  private final OrderUpdateUseCase orderUpdateUseCase;

  public Mono<Payment> execute(UUID orderId, UUID paymentMethodId, String cvv) {

    log.info(
        "Reintento con tarjeta guardada: orderId={}, paymentMethodId={}", orderId, paymentMethodId);

    return Mono.justOrEmpty(orderRepository.findById(new OrderId(orderId)))
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Orden no encontrada: " + orderId)))
        .flatMap(
            order -> {
              if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
                return Mono.error(
                    new PaymentAlreadyProcessedException(
                        "Solo se pueden reintentar pagos con estado PENDING_PAYMENT. Estado actual: "
                            + order.getStatus()));
              }

              if (cvv == null || cvv.isBlank()) {
                return Mono.error(
                    new CvvRequiredException("El CVV es requerido para tarjetas guardadas"));
              }

              return Mono.justOrEmpty(paymentMethodRepository.findById(paymentMethodId))
                  .switchIfEmpty(
                      Mono.error(
                          new com.eliteshop.colombia.payment.domain.exception
                              .PaymentNotFoundException(
                              "Metodo de pago no encontrado: " + paymentMethodId)))
                  .flatMap(
                      method -> {
                        Customer customer =
                            customerRepository
                                .findById(
                                    new com.eliteshop.colombia.customer.domain.model.CustomerId(
                                        order.getCustomerId().getValue()))
                                .orElseThrow(
                                    () ->
                                        new com.eliteshop.colombia.customer.domain.exception
                                            .CustomerNotFoundException(
                                            "Cliente no encontrado: "
                                                + order.getCustomerId().getValue()));

                        return Mono.justOrEmpty(paymentRepository.findByOrderId(orderId))
                            .switchIfEmpty(
                                Mono.error(
                                    new com.eliteshop.colombia.payment.domain.exception
                                        .PaymentNotFoundException(
                                        "No existe pago asociado a la orden: " + orderId)))
                            .flatMap(
                                existingPayment -> {
                                  String newInvoice =
                                      "INV-"
                                          + UUID.randomUUID()
                                              .toString()
                                              .substring(0, 12)
                                              .toUpperCase();

                                  return paymentGateway
                                      .chargeWithToken(
                                          method.getEpaycoToken().getValue(),
                                          method.getEpaycoCustomerId().getValue(),
                                          cvv,
                                          existingPayment.getAmount(),
                                          newInvoice,
                                          customer.getFirstName().getValue(),
                                          customer.getLastName().getValue(),
                                          customer.getEmail().getValue(),
                                          resolveDocType(
                                              method.getDocType() != null
                                                  ? method.getDocType().getValue()
                                                  : null),
                                          resolveDocNumber(
                                              method.getDocNumber() != null
                                                  ? method.getDocNumber().getValue()
                                                  : null,
                                              customer.getId().getValue()))
                                      .map(
                                          chargedPayment -> {
                                            existingPayment.setInvoice(newInvoice);
                                            existingPayment.setEpaycoRefId(
                                                chargedPayment.getEpaycoRefId());
                                            existingPayment.setStatus(chargedPayment.getStatus());
                                            existingPayment.setUpdatedAt(LocalDateTime.now());
                                            paymentRepository.save(existingPayment);

                                            log.info(
                                                "Pago con tarjeta guardada procesado: invoice={}, status={}, refId={}",
                                                newInvoice,
                                                chargedPayment.getStatus(),
                                                chargedPayment.getEpaycoRefId());

                                            if (chargedPayment.getStatus()
                                                == PaymentStatus.APPROVED) {
                                              Order updatedOrder =
                                                  new Order(
                                                      order.getId(),
                                                      order.getCustomerId(),
                                                      OrderStatus.PAID,
                                                      order.getTotalAmount(),
                                                      order.getShippingAddress(),
                                                      order.getShippingDepartment(),
                                                      order.getShippingCity(),
                                                      order.getCreatedAt(),
                                                      new OrderUpdatedAt(
                                                          new Timestamp(
                                                              System.currentTimeMillis())),
                                                      order.getTrackingNumber(),
                                                      order.getShippingCarrier(),
                                                      order.getShippingLabelUrl(),
                                                      null);
                                              orderUpdateUseCase.execute(updatedOrder);
                                              log.info("Orden {} actualizada a PAID", orderId);
                                            }

                                            return existingPayment;
                                          });
                                });
                      });
            });
  }

  private String resolveDocType(String docType) {
    if (docType == null || docType.isBlank()) return "CC";
    try {
      return docType.toUpperCase();
    } catch (Exception e) {
      return "CC";
    }
  }

  private String resolveDocNumber(String docNumber, UUID customerId) {
    if (docNumber == null || docNumber.isBlank()) return customerId.toString();
    return docNumber;
  }
}
