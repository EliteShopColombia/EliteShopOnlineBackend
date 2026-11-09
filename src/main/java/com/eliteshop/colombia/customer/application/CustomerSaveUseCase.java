package com.eliteshop.colombia.customer.application;

import com.eliteshop.colombia.customer.domain.exception.CustomerExistException;
import com.eliteshop.colombia.customer.domain.model.Customer;
import com.eliteshop.colombia.customer.domain.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomerSaveUseCase {

  private final CustomerRepository repository;

  public void execute(Customer customer) {
    repository
        .findById(customer.getId())
        .ifPresent(
            existingCustomer -> {
              throw new CustomerExistException("El cliente ya existe en la plataforma");
            });

    this.repository.save(customer);
  }
}
