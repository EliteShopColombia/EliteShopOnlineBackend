package com.eliteshop.colombia.auth.application;

import com.eliteshop.colombia.customer.domain.exception.CustomerExistException;
import com.eliteshop.colombia.customer.domain.model.Customer;
import com.eliteshop.colombia.customer.domain.model.CustomerPassword;
import com.eliteshop.colombia.customer.domain.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@RequiredArgsConstructor
public class RegisterUseCase {

  private final CustomerRepository repository;
  private final PasswordEncoder passwordEncoder;

  public void execute(Customer customer) {
    repository
        .findByEmail(customer.getEmail().getValue())
        .ifPresent(
            existing -> {
              throw new CustomerExistException("A customer with this email already exists");
            });

    Customer encoded =
        new Customer(
            customer.getId(),
            customer.getFirstName(),
            customer.getLastName(),
            customer.getEmail(),
            customer.getPhoneNumber(),
            new CustomerPassword(passwordEncoder.encode(customer.getPassword().getValue())),
            customer.getProfileImage(),
            customer.getCreatedAt(),
            customer.getUpdatedAt(),
            customer.getInfo());

    repository.save(encoded);
  }
}
