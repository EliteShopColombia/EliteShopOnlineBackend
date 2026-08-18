package com.eliteshop.colombia.payment.domain.port;

import com.eliteshop.colombia.payment.domain.model.*;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {
  Payment save(Payment payment);

  Optional<Payment> findById(UUID id);

  Optional<Payment> findByEpaycoRefId(String refId);

  Optional<Payment> findBySessionId(String sessionId);

  Optional<Payment> findByInvoice(String invoice);
}
