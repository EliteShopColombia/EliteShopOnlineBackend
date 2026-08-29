package com.eliteshop.colombia.customer.domain.repository;

import com.eliteshop.colombia.customer.domain.model.Customer;
import com.eliteshop.colombia.customer.domain.model.CustomerId;
import com.eliteshop.colombia.shared.domain.PageResult;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository {
  Customer save(Customer customer);

  void update(Customer customer);

  void delete(CustomerId id);

  List<Customer> findAll();

  PageResult<Customer> findPage(int page, int size);

  Optional<Customer> findById(CustomerId id);

  Optional<Customer> findByEmail(String email);
}
