package com.eliteshop.colombia.customer.application;

import com.eliteshop.colombia.customer.domain.exception.CustomerNotExistException;
import com.eliteshop.colombia.customer.domain.model.CustomerId;
import com.eliteshop.colombia.customer.domain.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomerDeleteUseCase {

  private final CustomerRepository repository;

  public void execute(CustomerId id) {
    if (!repository.findById(id).isPresent()) {
      throw new CustomerNotExistException("The customer not exist in our platform");
    }

    this.repository.delete(id);
  }
}
