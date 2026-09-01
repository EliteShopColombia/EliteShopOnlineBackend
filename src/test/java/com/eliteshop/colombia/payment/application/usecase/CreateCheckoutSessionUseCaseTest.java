package com.eliteshop.colombia.payment.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.eliteshop.colombia.payment.domain.exception.PaymentAlreadyProcessedException;
import com.eliteshop.colombia.payment.domain.model.*;
import com.eliteshop.colombia.payment.domain.port.PaymentGateway;
import com.eliteshop.colombia.payment.domain.port.PaymentRepository;
import com.eliteshop.colombia.payment.infrastructure.config.EpaycoProperties;
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
class CreateCheckoutSessionUseCaseTest {

  @Mock private PaymentGateway paymentGateway;

  @Mock private PaymentRepository paymentRepository;

  private CreateCheckoutSessionUseCase useCase;

  @BeforeEach
  void setUp() {
    EpaycoProperties epaycoProperties = new EpaycoProperties();
    epaycoProperties.setResponseUrl("http://localhost:5173/order");
    useCase = new CreateCheckoutSessionUseCase(paymentGateway, paymentRepository, epaycoProperties);
  }

  @Test
  void shouldCreatePaymentBeforeGatewayCall() {
    UUID orderId = UUID.randomUUID();
    String invoice = "INV-TEST123";

    CheckoutSessionRequest request =
        CheckoutSessionRequest.builder()
            .orderId(orderId)
            .amount(new BigDecimal("150000"))
            .customerEmail("test@email.com")
            .invoice(invoice)
            .paymentMethod("CARD")
            .build();

    when(paymentRepository.findByInvoice(invoice)).thenReturn(Optional.empty());
    when(paymentRepository.save(any(Payment.class)))
        .thenAnswer(
            invocation -> {
              Payment p = invocation.getArgument(0);
              if (p.getId() == null) p.setId(UUID.randomUUID());
              return p;
            });

    CheckoutSession session =
        CheckoutSession.builder().sessionId("session-123").token("token-abc").build();

    when(paymentGateway.createSession(any())).thenReturn(Mono.just(session));

    StepVerifier.create(useCase.execute(request))
        .assertNext(
            result -> {
              assertThat(result.getSessionId()).isEqualTo("session-123");
              assertThat(result.getToken()).isEqualTo("token-abc");
              assertThat(result.getInvoice()).isEqualTo(invoice);
            })
        .verifyComplete();

    verify(paymentRepository, times(2)).save(any(Payment.class));
  }

  @Test
  void shouldUsePaymentMethodFromRequest() {
    UUID orderId = UUID.randomUUID();
    String invoice = "INV-PSE-001";

    CheckoutSessionRequest request =
        CheckoutSessionRequest.builder()
            .orderId(orderId)
            .amount(new BigDecimal("200000"))
            .customerEmail("pse@email.com")
            .invoice(invoice)
            .paymentMethod("PSE")
            .build();

    when(paymentRepository.findByInvoice(invoice)).thenReturn(Optional.empty());
    when(paymentRepository.save(any(Payment.class)))
        .thenAnswer(
            invocation -> {
              Payment p = invocation.getArgument(0);
              if (p.getId() == null) p.setId(UUID.randomUUID());
              return p;
            });

    CheckoutSession session =
        CheckoutSession.builder().sessionId("session-pse").token("token-pse").build();

    when(paymentGateway.createSession(any())).thenReturn(Mono.just(session));

    StepVerifier.create(useCase.execute(request))
        .assertNext(
            result -> {
              assertThat(result.getSessionId()).isEqualTo("session-pse");
            })
        .verifyComplete();

    verify(paymentRepository, times(2))
        .save(argThat(payment -> payment.getMethod() == PaymentMethod.PSE));
  }

  @Test
  void shouldDefaultToCardWhenMethodIsNullOrBlank() {
    UUID orderId = UUID.randomUUID();
    String invoice = "INV-CARD-DEFAULT";

    CheckoutSessionRequest request =
        CheckoutSessionRequest.builder()
            .orderId(orderId)
            .amount(new BigDecimal("100000"))
            .customerEmail("card@email.com")
            .invoice(invoice)
            .paymentMethod(null)
            .build();

    when(paymentRepository.findByInvoice(invoice)).thenReturn(Optional.empty());
    when(paymentRepository.save(any(Payment.class)))
        .thenAnswer(
            invocation -> {
              Payment p = invocation.getArgument(0);
              if (p.getId() == null) p.setId(UUID.randomUUID());
              return p;
            });

    CheckoutSession session =
        CheckoutSession.builder().sessionId("session-card").token("token-card").build();

    when(paymentGateway.createSession(any())).thenReturn(Mono.just(session));

    StepVerifier.create(useCase.execute(request))
        .assertNext(result -> assertThat(result).isNotNull())
        .verifyComplete();

    verify(paymentRepository, times(2))
        .save(argThat(payment -> payment.getMethod() == PaymentMethod.CARD));
  }

  @Test
  void shouldRejectWhenPaymentAlreadyApproved() {
    UUID orderId = UUID.randomUUID();
    String invoice = "INV-APPROVED";

    CheckoutSessionRequest request =
        CheckoutSessionRequest.builder()
            .orderId(orderId)
            .amount(new BigDecimal("150000"))
            .customerEmail("test@email.com")
            .invoice(invoice)
            .build();

    Payment existingPayment =
        Payment.builder()
            .id(UUID.randomUUID())
            .orderId(orderId)
            .status(PaymentStatus.APPROVED)
            .invoice(invoice)
            .createdAt(LocalDateTime.now())
            .build();

    when(paymentRepository.findByInvoice(invoice)).thenReturn(Optional.of(existingPayment));

    StepVerifier.create(useCase.execute(request))
        .expectError(PaymentAlreadyProcessedException.class)
        .verify();

    verify(paymentGateway, never()).createSession(any());
  }

  @Test
  void shouldSetPaymentToErrorWhenGatewayFails() {
    UUID orderId = UUID.randomUUID();
    String invoice = "INV-FAIL";

    CheckoutSessionRequest request =
        CheckoutSessionRequest.builder()
            .orderId(orderId)
            .amount(new BigDecimal("150000"))
            .customerEmail("test@email.com")
            .invoice(invoice)
            .build();

    when(paymentRepository.findByInvoice(invoice)).thenReturn(Optional.empty());
    when(paymentRepository.save(any(Payment.class)))
        .thenAnswer(
            invocation -> {
              Payment p = invocation.getArgument(0);
              if (p.getId() == null) p.setId(UUID.randomUUID());
              return p;
            });

    when(paymentGateway.createSession(any()))
        .thenReturn(Mono.error(new RuntimeException("Gateway down")));

    StepVerifier.create(useCase.execute(request)).expectError(RuntimeException.class).verify();

    verify(paymentRepository, times(2))
        .save(argThat(payment -> payment.getStatus() == PaymentStatus.ERROR));
  }

  @Test
  void shouldRejectInvalidPaymentMethod() {
    UUID orderId = UUID.randomUUID();
    String invoice = "INV-INVALID-METHOD";

    CheckoutSessionRequest request =
        CheckoutSessionRequest.builder()
            .orderId(orderId)
            .amount(new BigDecimal("150000"))
            .customerEmail("test@email.com")
            .invoice(invoice)
            .paymentMethod("INVALID")
            .build();

    when(paymentRepository.findByInvoice(invoice)).thenReturn(Optional.empty());
    when(paymentRepository.save(any(Payment.class)))
        .thenAnswer(
            invocation -> {
              Payment p = invocation.getArgument(0);
              if (p.getId() == null) p.setId(UUID.randomUUID());
              return p;
            });

    CheckoutSession session =
        CheckoutSession.builder().sessionId("session-123").token("token-abc").build();

    when(paymentGateway.createSession(any())).thenReturn(Mono.just(session));

    StepVerifier.create(useCase.execute(request))
        .assertNext(result -> assertThat(result).isNotNull())
        .verifyComplete();

    verify(paymentRepository, times(2))
        .save(argThat(payment -> payment.getMethod() == PaymentMethod.CARD));
  }
}
