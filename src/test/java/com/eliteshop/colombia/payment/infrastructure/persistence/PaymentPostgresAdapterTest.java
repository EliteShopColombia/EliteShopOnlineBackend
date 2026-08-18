package com.eliteshop.colombia.payment.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.eliteshop.colombia.payment.domain.model.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentPostgresAdapterTest {

  @Mock private PaymentJpaRepository repository;

  @InjectMocks private PaymentPostgresAdapter adapter;

  @Test
  void shouldReturnPersistedEntityFromSave() {
    UUID paymentId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();

    Payment payment =
        Payment.builder()
            .id(paymentId)
            .orderId(orderId)
            .amount(new BigDecimal("150000"))
            .currency("COP")
            .method(PaymentMethod.CARD)
            .status(PaymentStatus.PENDING)
            .invoice("INV-TEST")
            .customerEmail("test@email.com")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    PaymentEntity savedEntity = new PaymentEntity();
    savedEntity.setId(paymentId);
    savedEntity.setOrderId(orderId);
    savedEntity.setAmount(new BigDecimal("150000"));
    savedEntity.setCurrency("COP");
    savedEntity.setPaymentMethod("CARD");
    savedEntity.setPaymentStatus("PENDING");
    savedEntity.setInvoice("INV-TEST");
    savedEntity.setCustomerEmail("test@email.com");
    savedEntity.setCreatedAt(payment.getCreatedAt());
    savedEntity.setUpdatedAt(payment.getUpdatedAt());

    when(repository.save(any(PaymentEntity.class))).thenReturn(savedEntity);

    Payment result = adapter.save(payment);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(paymentId);
    assertThat(result.getOrderId()).isEqualTo(orderId);
    assertThat(result.getStatus()).isEqualTo(PaymentStatus.PENDING);
    assertThat(result.getMethod()).isEqualTo(PaymentMethod.CARD);
  }

  @Test
  void shouldMapNullStatusToPENDING() {
    Payment payment =
        Payment.builder()
            .id(UUID.randomUUID())
            .orderId(UUID.randomUUID())
            .amount(new BigDecimal("100000"))
            .currency("COP")
            .status(null)
            .build();

    PaymentEntity savedEntity = new PaymentEntity();
    savedEntity.setId(payment.getId());
    savedEntity.setOrderId(payment.getOrderId());
    savedEntity.setAmount(payment.getAmount());
    savedEntity.setCurrency("COP");
    savedEntity.setPaymentStatus("PENDING");

    when(repository.save(any(PaymentEntity.class))).thenReturn(savedEntity);

    Payment result = adapter.save(payment);

    assertThat(result.getStatus()).isEqualTo(PaymentStatus.PENDING);
  }

  @Test
  void shouldMapNullMethodGracefully() {
    Payment payment =
        Payment.builder()
            .id(UUID.randomUUID())
            .orderId(UUID.randomUUID())
            .amount(new BigDecimal("100000"))
            .currency("COP")
            .method(null)
            .status(PaymentStatus.PENDING)
            .build();

    PaymentEntity savedEntity = new PaymentEntity();
    savedEntity.setId(payment.getId());
    savedEntity.setOrderId(payment.getOrderId());
    savedEntity.setAmount(payment.getAmount());
    savedEntity.setCurrency("COP");
    savedEntity.setPaymentMethod(null);
    savedEntity.setPaymentStatus("PENDING");

    when(repository.save(any(PaymentEntity.class))).thenReturn(savedEntity);

    Payment result = adapter.save(payment);

    assertThat(result.getMethod()).isNull();
  }

  @Test
  void shouldFindById() {
    UUID id = UUID.randomUUID();
    PaymentEntity entity = new PaymentEntity();
    entity.setId(id);
    entity.setOrderId(UUID.randomUUID());
    entity.setAmount(new BigDecimal("100000"));
    entity.setCurrency("COP");
    entity.setPaymentStatus("APPROVED");
    entity.setInvoice("INV-FIND");

    when(repository.findById(id)).thenReturn(Optional.of(entity));

    Optional<Payment> result = adapter.findById(id);

    assertThat(result).isPresent();
    assertThat(result.get().getStatus()).isEqualTo(PaymentStatus.APPROVED);
    assertThat(result.get().getInvoice()).isEqualTo("INV-FIND");
  }

  @Test
  void shouldFindByEpaycoRefId() {
    String refId = "ref-123";
    PaymentEntity entity = new PaymentEntity();
    entity.setId(UUID.randomUUID());
    entity.setOrderId(UUID.randomUUID());
    entity.setAmount(new BigDecimal("200000"));
    entity.setCurrency("COP");
    entity.setPaymentStatus("PENDING");
    entity.setEpaycoRefId(refId);

    when(repository.findByEpaycoRefId(refId)).thenReturn(Optional.of(entity));

    Optional<Payment> result = adapter.findByEpaycoRefId(refId);

    assertThat(result).isPresent();
    assertThat(result.get().getEpaycoRefId()).isEqualTo(refId);
  }
}
