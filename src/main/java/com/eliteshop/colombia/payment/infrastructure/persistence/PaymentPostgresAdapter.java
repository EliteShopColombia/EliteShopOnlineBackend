package com.eliteshop.colombia.payment.infrastructure.persistence;

import com.eliteshop.colombia.payment.domain.model.*;
import com.eliteshop.colombia.payment.domain.port.PaymentRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentPostgresAdapter implements PaymentRepository {

  private final PaymentJpaRepository repository;

  @Override
  public Payment save(Payment payment) {
    PaymentEntity entity = toEntity(payment);
    PaymentEntity savedEntity = repository.save(entity);
    return toDomain(savedEntity);
  }

  @Override
  public Optional<Payment> findById(UUID id) {
    return repository.findById(id).map(this::toDomain);
  }

  @Override
  public Optional<Payment> findByEpaycoRefId(String refId) {
    return repository.findByEpaycoRefId(refId).map(this::toDomain);
  }

  @Override
  public Optional<Payment> findBySessionId(String sessionId) {
    return repository.findBySessionId(sessionId).map(this::toDomain);
  }

  @Override
  public Optional<Payment> findByInvoice(String invoice) {
    return repository.findByInvoice(invoice).map(this::toDomain);
  }

  @Override
  public Optional<Payment> findByOrderId(UUID orderId) {
    return repository.findByOrderId(orderId).map(this::toDomain);
  }

  private PaymentEntity toEntity(Payment payment) {
    PaymentEntity entity = new PaymentEntity();
    entity.setId(payment.getId());
    entity.setOrderId(payment.getOrderId());
    entity.setSellerId(payment.getSellerId());
    entity.setAmount(payment.getAmount());
    entity.setCurrency(payment.getCurrency());
    entity.setPaymentMethod(payment.getMethod() != null ? payment.getMethod().name() : null);
    entity.setPaymentStatus(
        payment.getStatus() != null ? payment.getStatus().name() : PaymentStatus.PENDING.name());
    entity.setEpaycoRefId(payment.getEpaycoRefId());
    entity.setSessionId(payment.getSessionId());
    entity.setInvoice(payment.getInvoice());
    entity.setCustomerEmail(payment.getCustomerEmail());
    entity.setPlatformFee(payment.getPlatformFee());
    entity.setSellerAmount(payment.getSellerAmount());
    entity.setCreatedAt(payment.getCreatedAt());
    entity.setUpdatedAt(payment.getUpdatedAt());
    return entity;
  }

  private Payment toDomain(PaymentEntity entity) {
    return Payment.builder()
        .id(entity.getId())
        .orderId(entity.getOrderId())
        .sellerId(entity.getSellerId())
        .amount(entity.getAmount())
        .currency(entity.getCurrency())
        .method(
            entity.getPaymentMethod() != null
                ? PaymentMethod.valueOf(entity.getPaymentMethod())
                : null)
        .status(
            entity.getPaymentStatus() != null
                ? PaymentStatus.valueOf(entity.getPaymentStatus())
                : PaymentStatus.PENDING)
        .epaycoRefId(entity.getEpaycoRefId())
        .sessionId(entity.getSessionId())
        .invoice(entity.getInvoice())
        .customerEmail(entity.getCustomerEmail())
        .platformFee(entity.getPlatformFee())
        .sellerAmount(entity.getSellerAmount())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
  }
}
