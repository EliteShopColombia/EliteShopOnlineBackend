package com.eliteshop.colombia.customer.application;

import com.eliteshop.colombia.customer.domain.model.Customer;
import com.eliteshop.colombia.customer.domain.model.CustomerId;
import com.eliteshop.colombia.customer.domain.repository.CustomerRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CustomerFindByIdUseCase {

  private final CustomerRepository repository;

  public Optional<Customer> execute(CustomerId id) {
    log.info("Buscando cliente con id: {}", id);

    if (id == null) {
      log.warn("Id del cliente es null");
      return Optional.empty();
    }

    Optional<Customer> customer = repository.findById(id);
    if (customer.isPresent()) {
      log.info("Cliente encontrado con id: {}", id);
    } else {
      log.info("Cliente no encontrado con id: {}", id);
    }
    return customer;
  }
}
