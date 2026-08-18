package com.eliteshop.colombia.payment.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.eliteshop.colombia.payment.domain.exception.PaymentNotFoundException;
import com.eliteshop.colombia.payment.domain.model.*;
import com.eliteshop.colombia.payment.domain.port.PaymentGateway;
import com.eliteshop.colombia.payment.domain.port.PaymentRepository;
import java.math.BigDecimal;
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
class ConfirmPaymentUseCaseTest {

  @Mock private PaymentGateway paymentGateway;

  @Mock private PaymentRepository paymentRepository;

  private ConfirmPaymentUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new ConfirmPaymentUseCase(paymentGateway, paymentRepository);
  }

  @Test
  void shouldUpdatePaymentStatusOnConfirmation() {
    String refId = "ref-123";

    Payment existingPayment =
        Payment.builder()
            .id(UUID.randomUUID())
            .orderId(UUID.randomUUID())
            .status(PaymentStatus.PENDING)
            .epaycoRefId(null)
            .invoice("INV-TEST")
            .amount(new BigDecimal("150000"))
            .createdAt(LocalDateTime.now())
            .build();

    Payment epaycoResponse =
        Payment.builder().epaycoRefId(refId).status(PaymentStatus.APPROVED).build();

    when(paymentGateway.confirmTransaction(refId)).thenReturn(Mono.just(epaycoResponse));
    when(paymentRepository.findByEpaycoRefId(refId)).thenReturn(Optional.of(existingPayment));
    when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

    StepVerifier.create(useCase.execute(refId))
        .assertNext(
            payment -> {
              assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
              assertThat(payment.getEpaycoRefId()).isEqualTo(refId);
              assertThat(payment.getUpdatedAt()).isNotNull();
            })
        .verifyComplete();

    verify(paymentRepository).save(any(Payment.class));
  }

  @Test
  void shouldReturnExistingPaymentWhenAlreadyApproved() {
    String refId = "ref-approved";

    Payment existingPayment =
        Payment.builder()
            .id(UUID.randomUUID())
            .status(PaymentStatus.APPROVED)
            .epaycoRefId(refId)
            .createdAt(LocalDateTime.now())
            .build();

    Payment epaycoResponse =
        Payment.builder().epaycoRefId(refId).status(PaymentStatus.APPROVED).build();

    when(paymentGateway.confirmTransaction(refId)).thenReturn(Mono.just(epaycoResponse));
    when(paymentRepository.findByEpaycoRefId(refId)).thenReturn(Optional.of(existingPayment));

    StepVerifier.create(useCase.execute(refId))
        .assertNext(
            payment -> {
              assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
            })
        .verifyComplete();

    verify(paymentRepository, never()).save(any());
  }

  @Test
  void shouldReturnErrorWhenPaymentNotFound() {
    String refId = "ref-notfound";

    Payment epaycoResponse =
        Payment.builder().epaycoRefId(refId).status(PaymentStatus.APPROVED).build();

    when(paymentGateway.confirmTransaction(refId)).thenReturn(Mono.just(epaycoResponse));
    when(paymentRepository.findByEpaycoRefId(refId)).thenReturn(Optional.empty());

    StepVerifier.create(useCase.execute(refId))
        .expectError(PaymentNotFoundException.class)
        .verify();
  }

  @Test
  void shouldReturnPaymentByInvoice() {
    String invoice = "INV-123";

    Payment payment =
        Payment.builder()
            .id(UUID.randomUUID())
            .invoice(invoice)
            .status(PaymentStatus.PENDING)
            .build();

    when(paymentRepository.findByInvoice(invoice)).thenReturn(Optional.of(payment));

    StepVerifier.create(useCase.getPaymentByInvoice(invoice))
        .assertNext(
            result -> {
              assertThat(result.getInvoice()).isEqualTo(invoice);
              assertThat(result.getStatus()).isEqualTo(PaymentStatus.PENDING);
            })
        .verifyComplete();
  }

  @Test
  void shouldReturnErrorWhenInvoiceNotFound() {
    when(paymentRepository.findByInvoice("INV-NONE")).thenReturn(Optional.empty());

    StepVerifier.create(useCase.getPaymentByInvoice("INV-NONE"))
        .expectError(PaymentNotFoundException.class)
        .verify();
  }

  @Test
  void shouldHandleGatewayError() {
    String refId = "ref-error";

    when(paymentGateway.confirmTransaction(refId))
        .thenReturn(Mono.error(new RuntimeException("Gateway timeout")));

    StepVerifier.create(useCase.execute(refId)).expectError(RuntimeException.class).verify();
  }
}
