package com.eliteshop.colombia.payment.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, UUID> {
  Optional<PaymentEntity> findByEpaycoRefId(String epaycoRefId);

  Optional<PaymentEntity> findBySessionId(String sessionId);

  Optional<PaymentEntity> findByInvoice(String invoice);

  Optional<PaymentEntity> findByOrderId(UUID orderId);
}
