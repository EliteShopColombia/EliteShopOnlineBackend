package com.eliteshop.colombia.auth.application;

import com.eliteshop.colombia.auth.domain.exception.InvalidCredentialsException;
import com.eliteshop.colombia.customer.domain.model.Customer;
import com.eliteshop.colombia.customer.domain.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@RequiredArgsConstructor
public class LoginUseCase {

  private final CustomerRepository customerRepository;
  private final PasswordEncoder passwordEncoder;

  public Customer execute(String email, String password) {
    log.info("Iniciando login para email: {}", email);
    try {
      Customer customer =
          customerRepository
              .findByEmail(email)
              .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

      if (!passwordEncoder.matches(password, customer.getPassword().getValue())) {
        throw new InvalidCredentialsException("Invalid email or password");
      }

      log.info("Login exitoso para email: {}", email);
      return customer;
    } catch (InvalidCredentialsException e) {
      log.error("Error en login para email {}: {}", email, e.getMessage());
      throw e;
    }
  }
}
