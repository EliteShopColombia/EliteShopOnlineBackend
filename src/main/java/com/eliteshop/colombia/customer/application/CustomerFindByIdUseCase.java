package com.eliteshop.colombia.customer.application;

import com.eliteshop.colombia.customer.domain.model.Customer;
import com.eliteshop.colombia.customer.domain.model.CustomerId;
import com.eliteshop.colombia.customer.domain.repository.CustomerRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomerFindByIdUseCase {

  private final CustomerRepository repository;

  public Optional<Customer> execute(CustomerId id) {
    if (id == null) return Optional.empty();

    return repository.findById(id);
  }
}
