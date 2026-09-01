package com.eliteshop.colombia.payment.application.usecase;

import com.eliteshop.colombia.payment.domain.exception.PaymentNotFoundException;
import com.eliteshop.colombia.payment.domain.model.paymentmethod.CustomerPaymentMethod;
import com.eliteshop.colombia.payment.domain.port.CustomerPaymentMethodRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DeletePaymentMethodUseCase {

  private final CustomerPaymentMethodRepository repository;

  public void execute(UUID customerId, UUID paymentMethodId) {
    log.info("Intentando eliminar método de pago {} para cliente {}", paymentMethodId, customerId);
    try {
      CustomerPaymentMethod method =
          repository
              .findById(paymentMethodId)
              .orElseThrow(() -> new PaymentNotFoundException("Método de pago no encontrado"));

      if (!method.getCustomerId().getValue().equals(customerId)) {
        throw new IllegalArgumentException("No tienes permiso para eliminar este método de pago");
      }

      repository.deleteById(paymentMethodId);
      log.info(
          "Método de pago {} eliminado exitosamente para cliente {}", paymentMethodId, customerId);
    } catch (PaymentNotFoundException | IllegalArgumentException e) {
      log.error(
          "Error eliminando método de pago {} para cliente {}: {}",
          paymentMethodId,
          customerId,
          e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error(
          "Error inesperado eliminando método de pago {} para cliente {}: {}",
          paymentMethodId,
          customerId,
          e.getMessage());
      throw e;
    }
  }
}
