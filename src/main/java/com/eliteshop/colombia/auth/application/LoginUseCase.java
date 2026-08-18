package com.eliteshop.colombia.auth.application;

import com.eliteshop.colombia.auth.domain.exception.InvalidCredentialsException;
import com.eliteshop.colombia.customer.domain.model.Customer;
import com.eliteshop.colombia.customer.domain.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@RequiredArgsConstructor
public class LoginUseCase {

  private final CustomerRepository customerRepository;
  private final PasswordEncoder passwordEncoder;

  public Customer execute(String email, String password) {
    Customer customer =
        customerRepository
            .findByEmail(email)
            .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

    if (!passwordEncoder.matches(password, customer.getPassword().getValue())) {
      throw new InvalidCredentialsException("Invalid email or password");
    }

    return customer;
  }
}
