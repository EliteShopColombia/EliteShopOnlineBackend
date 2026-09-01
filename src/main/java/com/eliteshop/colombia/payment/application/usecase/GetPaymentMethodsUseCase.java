package com.eliteshop.colombia.payment.application.usecase;

import com.eliteshop.colombia.payment.domain.model.paymentmethod.CustomerPaymentMethod;
import com.eliteshop.colombia.payment.domain.port.CustomerPaymentMethodRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class GetPaymentMethodsUseCase {

  private final CustomerPaymentMethodRepository repository;

  public List<CustomerPaymentMethod> execute(UUID customerId) {
    log.info("Consultando métodos de pago del cliente {}", customerId);
    try {
      List<CustomerPaymentMethod> methods = repository.findByCustomerId(customerId);
      log.info("Se encontraron {} métodos de pago para cliente {}", methods.size(), customerId);
      return methods;
    } catch (Exception e) {
      log.error(
          "Error consultando métodos de pago para cliente {}: {}", customerId, e.getMessage());
      throw e;
    }
  }
}
