package com.eliteshop.colombia.auth.application;

import com.eliteshop.colombia.customer.domain.exception.CustomerExistException;
import com.eliteshop.colombia.customer.domain.model.Customer;
import com.eliteshop.colombia.customer.domain.model.CustomerPassword;
import com.eliteshop.colombia.customer.domain.repository.CustomerRepository;
import com.eliteshop.colombia.shared.domain.LocationValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@RequiredArgsConstructor
public class RegisterUseCase {

  private final CustomerRepository repository;
  private final PasswordEncoder passwordEncoder;
  private final LocationValidationService locationValidationService;

  public void execute(Customer customer) {
    log.info("Iniciando registro para email: {}", customer.getEmail().getValue());

    // Validate location if customer provided DNI info with department/city
    if (customer.getInfo() != null) {
      String department = customer.getInfo().getDepartment().getValue();
      String city = customer.getInfo().getCity().getValue();
      locationValidationService.validateLocation(department, city);
    }

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
  }
}
