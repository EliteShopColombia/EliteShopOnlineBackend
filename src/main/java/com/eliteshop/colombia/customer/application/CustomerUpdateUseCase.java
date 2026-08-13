package com.eliteshop.colombia.customer.application;

import com.eliteshop.colombia.customer.domain.exception.CustomerNotExistException;
import com.eliteshop.colombia.customer.domain.model.Customer;
import com.eliteshop.colombia.customer.domain.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomerUpdateUseCase {

  private final CustomerRepository repository;

  public void execute(Customer customer) {
    if (customer.getId() == null || repository.findById(customer.getId()).isEmpty()) {
      throw new CustomerNotExistException("The customer not exist in our platform");
    }

    this.repository.update(customer);
  }
}
