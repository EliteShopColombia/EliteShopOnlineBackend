package com.eliteshop.colombia.payment.application.usecase;

import com.eliteshop.colombia.payment.domain.exception.PaymentNotFoundException;
import com.eliteshop.colombia.payment.domain.model.paymentmethod.CustomerPaymentMethod;
import com.eliteshop.colombia.payment.domain.port.CustomerPaymentMethodRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SetDefaultPaymentMethodUseCase {

  private final CustomerPaymentMethodRepository repository;

  public CustomerPaymentMethod execute(UUID customerId, UUID paymentMethodId) {
    log.info(
        "Estableciendo método de pago {} como predeterminado para cliente {}",
        paymentMethodId,
        customerId);
    try {
      CustomerPaymentMethod method =
          repository
              .findById(paymentMethodId)
              .orElseThrow(() -> new PaymentNotFoundException("Método de pago no encontrado"));

      if (!method.getCustomerId().getValue().equals(customerId)) {
        throw new IllegalArgumentException("No tienes permiso para modificar este método de pago");
      }

      repository.clearDefault(customerId);
      CustomerPaymentMethod updated = repository.save(method.asDefault());
      log.info(
          "Método de pago {} establecido como predeterminado para cliente {}",
          paymentMethodId,
          customerId);
      return updated;
    } catch (PaymentNotFoundException | IllegalArgumentException e) {
      log.error(
          "Error estableciendo método de pago predeterminado {} para cliente {}: {}",
          paymentMethodId,
          customerId,
          e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error(
          "Error inesperado estableciendo método de pago predeterminado {} para cliente {}: {}",
          paymentMethodId,
          customerId,
          e.getMessage());
      throw e;
    }
  }
}
