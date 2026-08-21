package com.eliteshop.colombia.auth.application;

import com.eliteshop.colombia.customer.domain.exception.CustomerExistException;
import com.eliteshop.colombia.customer.domain.model.Customer;
import com.eliteshop.colombia.customer.domain.model.CustomerPassword;
import com.eliteshop.colombia.customer.domain.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@RequiredArgsConstructor
public class RegisterUseCase {

  private final CustomerRepository repository;
  private final PasswordEncoder passwordEncoder;

  public void execute(Customer customer) {
    log.info("Iniciando registro para email: {}", customer.getEmail().getValue());
    try {
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
      log.info("Registro exitoso para email: {}", customer.getEmail().getValue());
    } catch (CustomerExistException e) {
      log.error(
          "Error en registro para email {}: {}", customer.getEmail().getValue(), e.getMessage());
      throw e;
    }
  }
}
