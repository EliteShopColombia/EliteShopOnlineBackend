package com.eliteshop.colombia.payment.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.eliteshop.colombia.payment.domain.exception.PaymentAlreadyProcessedException;
import com.eliteshop.colombia.payment.domain.model.*;
import com.eliteshop.colombia.payment.domain.port.PaymentGateway;
import com.eliteshop.colombia.payment.domain.port.PaymentRepository;
import com.eliteshop.colombia.payment.infrastructure.config.EpaycoProperties;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class PaymentIdempotencyTest {

  @Mock private PaymentGateway paymentGateway;
  @Mock private PaymentRepository paymentRepository;
  @Mock private EpaycoProperties epaycoProperties;
  private ConfirmPaymentUseCase confirmPaymentUseCase;
  private CreateCheckoutSessionUseCase createCheckoutSessionUseCase;

  @BeforeEach
  void setUp() {
    confirmPaymentUseCase = new ConfirmPaymentUseCase(paymentGateway, paymentRepository);
    createCheckoutSessionUseCase =
        new CreateCheckoutSessionUseCase(paymentGateway, paymentRepository, epaycoProperties);
  }

  @Test
  void shouldSkipUpdateWhenAlreadyApproved() {
    String refId = "ref-123";
    Payment existingPayment =
        Payment.builder()
            .id(UUID.randomUUID())
            .orderId(UUID.randomUUID())
            .status(PaymentStatus.APPROVED)
            .epaycoRefId(refId)
            .invoice("INV-123")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    Payment epaycoResponse =
        Payment.builder().status(PaymentStatus.APPROVED).epaycoRefId(refId).build();

    when(paymentGateway.confirmTransaction(refId)).thenReturn(Mono.just(epaycoResponse));
    when(paymentRepository.findByEpaycoRefId(refId)).thenReturn(Optional.of(existingPayment));

    StepVerifier.create(confirmPaymentUseCase.execute(refId))
        .assertNext(payment -> assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED))
        .verifyComplete();

    verify(paymentRepository, never()).save(any());
  }

  @Test
  void shouldUpdateWhenPaymentIsPending() {
    String refId = "ref-456";
    Payment existingPayment =
        Payment.builder()
            .id(UUID.randomUUID())
            .orderId(UUID.randomUUID())
            .status(PaymentStatus.PENDING)
            .epaycoRefId(null)
            .build();

    Payment epaycoResponse =
        Payment.builder().status(PaymentStatus.APPROVED).epaycoRefId(refId).build();

    Payment savedPayment =
        Payment.builder()
            .id(existingPayment.getId())
            .orderId(existingPayment.getOrderId())
            .status(PaymentStatus.APPROVED)
            .epaycoRefId(refId)
            .build();

    when(paymentGateway.confirmTransaction(refId)).thenReturn(Mono.just(epaycoResponse));
    when(paymentRepository.findByEpaycoRefId(refId)).thenReturn(Optional.of(existingPayment));
    when(paymentRepository.save(any())).thenReturn(savedPayment);

    StepVerifier.create(confirmPaymentUseCase.execute(refId))
        .assertNext(
            payment -> {
              assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
              verify(paymentRepository).save(any());
            })
        .verifyComplete();
  }

  @Test
  void shouldThrowWhenPaymentNotFound() {
    String refId = "ref-999";
    Payment epaycoResponse =
        Payment.builder().status(PaymentStatus.APPROVED).epaycoRefId(refId).build();

    when(paymentGateway.confirmTransaction(refId)).thenReturn(Mono.just(epaycoResponse));
    when(paymentRepository.findByEpaycoRefId(refId)).thenReturn(Optional.empty());

    StepVerifier.create(confirmPaymentUseCase.execute(refId)).expectError().verify();
  }

  @Test
  void shouldThrowWhenInvoiceAlreadyApproved() {
    UUID orderId = UUID.randomUUID();
    String existingInvoice = "INV-EXISTING";

    Payment existingPayment =
        Payment.builder()
            .id(UUID.randomUUID())
            .orderId(orderId)
            .invoice(existingInvoice)
            .status(PaymentStatus.APPROVED)
            .createdAt(LocalDateTime.now())
            .build();

    when(paymentRepository.findByInvoice(existingInvoice)).thenReturn(Optional.of(existingPayment));

    CheckoutSessionRequest request =
        CheckoutSessionRequest.builder()
            .orderId(orderId)
            .amount(new java.math.BigDecimal("100000"))
            .currency("COP")
            .invoice(existingInvoice)
            .customerEmail("test@example.com")
            .build();

    StepVerifier.create(createCheckoutSessionUseCase.execute(request))
        .expectError(PaymentAlreadyProcessedException.class)
        .verify();
  }
}
