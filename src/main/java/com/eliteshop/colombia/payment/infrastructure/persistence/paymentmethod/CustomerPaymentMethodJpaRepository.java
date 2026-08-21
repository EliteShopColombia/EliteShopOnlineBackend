package com.eliteshop.colombia.payment.infrastructure.persistence.paymentmethod;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerPaymentMethodJpaRepository
    extends JpaRepository<CustomerPaymentMethodEntity, UUID> {
  List<CustomerPaymentMethodEntity> findByCustomerId(UUID customerId);

  Optional<CustomerPaymentMethodEntity> findFirstByCustomerIdAndIsDefaultTrue(UUID customerId);

  void deleteByCustomerId(UUID customerId);
}
