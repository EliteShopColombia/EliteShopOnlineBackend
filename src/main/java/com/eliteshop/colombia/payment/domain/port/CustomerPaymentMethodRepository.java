package com.eliteshop.colombia.payment.domain.port;

import com.eliteshop.colombia.payment.domain.model.paymentmethod.CustomerPaymentMethod;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerPaymentMethodRepository {
  CustomerPaymentMethod save(CustomerPaymentMethod paymentMethod);

  List<CustomerPaymentMethod> findByCustomerId(UUID customerId);

  Optional<CustomerPaymentMethod> findById(UUID id);

  void deleteById(UUID id);

  void clearDefault(UUID customerId);

  Optional<CustomerPaymentMethod> findByDefault(UUID customerId);
}
