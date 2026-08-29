package com.eliteshop.colombia.customer.application;

import com.eliteshop.colombia.customer.domain.exception.CustomerNotFoundException;
import com.eliteshop.colombia.customer.domain.model.Customer;
import com.eliteshop.colombia.customer.domain.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CustomerUpdateUseCase {

  private final CustomerRepository repository;

  public void execute(Customer customer) {
    log.info("Iniciando actualización de cliente con id: {}", customer.getId());

    if (customer.getId() == null || repository.findById(customer.getId()).isEmpty()) {
      log.error("Cliente no encontrado para actualización con id: {}", customer.getId());
      throw new CustomerNotFoundException("Customer no encontrado con id: " + customer.getId());
    }

    this.repository.update(customer);
    log.info(
        "Cliente actualizado exitosaif (customer.getId() == null || repository.findBymente con id: {}",
        customer.getId());
  }
}
