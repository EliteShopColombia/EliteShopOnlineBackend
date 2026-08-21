package com.eliteshop.colombia.payment.application.usecase;

import com.eliteshop.colombia.customer.domain.model.Customer;
import com.eliteshop.colombia.customer.domain.model.CustomerId;
import com.eliteshop.colombia.customer.domain.repository.CustomerRepository;
import com.eliteshop.colombia.payment.domain.model.paymentmethod.*;
import com.eliteshop.colombia.payment.domain.port.CustomerPaymentMethodRepository;
import com.eliteshop.colombia.payment.domain.port.PaymentGateway;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class SavePaymentMethodUseCase {

  private final PaymentGateway paymentGateway;
  private final CustomerPaymentMethodRepository repository;
  private final CustomerRepository customerRepository;

  public Mono<CustomerPaymentMethod> execute(
      UUID customerId,
      String cardNumber,
      String cvc,
      int expiryMonth,
      int expiryYear,
      String docType,
      String docNumber,
      boolean setDefault) {

    log.info("Guardando método de pago para cliente {}", customerId);
    try {
      Customer customer =
          customerRepository
              .findById(new CustomerId(customerId))
              .orElseThrow(() -> new IllegalStateException("Customer no encontrado"));

      return paymentGateway
          .tokenizeCard(cardNumber, cvc, expiryMonth, expiryYear)
          .flatMap(
              tokenized -> {
                String existingEpaycoCustomerId =
                    repository.findByCustomerId(customerId).stream()
                        .map(method -> method.getEpaycoCustomerId().getValue())
                        .findFirst()
                        .orElse(null);

                Mono<String> epaycoCustomerMono =
                    existingEpaycoCustomerId != null
                        ? paymentGateway
                            .addTokenToCustomer(existingEpaycoCustomerId, tokenized.getToken())
                            .thenReturn(existingEpaycoCustomerId)
                        : paymentGateway.createEpaycoCustomer(
                            tokenized.getToken(),
                            customer.getFirstName().getValue(),
                            customer.getLastName().getValue(),
                            customer.getEmail().getValue(),
                            customer.getPhoneNumber().getValue(),
                            docType,
                            docNumber);

                return epaycoCustomerMono.map(
                    epaycoCustomerId -> {
                      CustomerPaymentMethod method =
                          CustomerPaymentMethod.create(
                              new CustomerPaymentMethodCustomerId(customerId),
                              new CustomerPaymentMethodEpaycoToken(tokenized.getToken()),
                              new CustomerPaymentMethodEpaycoCustomerId(epaycoCustomerId),
                              new CustomerPaymentMethodLast4(tokenized.getLast4()),
                              new CustomerPaymentMethodBrand(tokenized.getBrand()),
                              new CustomerPaymentMethodExpiryMonth(tokenized.getExpiryMonth()),
                              new CustomerPaymentMethodExpiryYear(tokenized.getExpiryYear()),
                              new CustomerPaymentMethodDocType(docType),
                              new CustomerPaymentMethodDocNumber(docNumber),
                              setDefault);
                      log.info("Método de pago guardado exitosamente para cliente {}", customerId);
                      return repository.save(method);
                    });
              })
          .doOnError(
              e ->
                  log.error(
                      "Error guardando método de pago para cliente {}: {}",
                      customerId,
                      e.getMessage()));
    } catch (Exception e) {
      log.error(
          "Error inesperado guardando método de pago para cliente {}: {}",
          customerId,
          e.getMessage());
      throw e;
    }
  }
}
