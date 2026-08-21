package com.eliteshop.colombia.customer.application;

import com.eliteshop.colombia.customer.domain.model.Customer;
import com.eliteshop.colombia.customer.domain.repository.CustomerRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CustomerFindAllUseCase {

  private final CustomerRepository repository;

  public List<Customer> execute() {
    log.info("Buscando todos los clientes");
    List<Customer> customers = repository.findAll();
    log.info("Se encontraron {} clientes", customers.size());
    return customers;
  }
}
