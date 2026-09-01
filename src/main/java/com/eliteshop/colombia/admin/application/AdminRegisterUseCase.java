package com.eliteshop.colombia.admin.application;

import com.eliteshop.colombia.customer.domain.exception.CustomerExistException;
import com.eliteshop.colombia.customer.domain.model.Customer;
import com.eliteshop.colombia.customer.domain.model.CustomerPassword;
import com.eliteshop.colombia.customer.domain.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@RequiredArgsConstructor
public class AdminRegisterUseCase {

  private final CustomerRepository repository;
  private final PasswordEncoder passwordEncoder;

  public Customer execute(Customer customer) {
    log.info("Iniciando registro de admin para email: {}", customer.getEmail().getValue());

    repository
        .findByEmail(customer.getEmail().getValue())
        .ifPresent(
            existing -> {
              throw new CustomerExistException("Ya existe un usuario con este email");
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
            customer.getRole(),
            customer.getCreatedAt(),
            customer.getUpdatedAt(),
            customer.getInfo());

    Customer saved = repository.save(encoded);
    log.info("Admin registrado exitosamente para email: {}", customer.getEmail().getValue());
    return saved;
  }
}
