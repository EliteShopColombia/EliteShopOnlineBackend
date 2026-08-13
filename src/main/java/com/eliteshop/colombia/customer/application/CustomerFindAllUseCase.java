package com.eliteshop.colombia.customer.application;

import com.eliteshop.colombia.customer.domain.model.Customer;
import com.eliteshop.colombia.customer.domain.repository.CustomerRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomerFindAllUseCase {

  private final CustomerRepository repository;

  public List<Customer> execute() {
    return repository.findAll();
  }
}
