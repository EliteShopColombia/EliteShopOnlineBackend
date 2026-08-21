package com.eliteshop.colombia.customer.infrastructure.controller;

import com.eliteshop.colombia.customer.application.CustomerDeleteUseCase;
import com.eliteshop.colombia.customer.application.CustomerFindAllUseCase;
import com.eliteshop.colombia.customer.application.CustomerFindByIdUseCase;
import com.eliteshop.colombia.customer.application.CustomerUpdateUseCase;
import com.eliteshop.colombia.customer.domain.model.*;
import com.eliteshop.colombia.customer.infrastructure.controller.dto.CustomerResponse;
import com.eliteshop.colombia.customer.infrastructure.controller.dto.UpdateCustomerRequest;
import com.eliteshop.colombia.customer.infrastructure.mapper.CustomerMapper;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

  private final CustomerUpdateUseCase updateUseCase;
  private final CustomerDeleteUseCase deleteUseCase;
  private final CustomerFindAllUseCase findAllUseCase;
  private final CustomerFindByIdUseCase findByIdUseCase;
  private final CustomerMapper mapper;

  @PutMapping("/{id}")
  public ResponseEntity<CustomerResponse> update(
      @PathVariable UUID id, @Valid @RequestBody UpdateCustomerRequest request) {
    Customer existing = findByIdUseCase.execute(new CustomerId(id)).orElseThrow();

    CustomerInfo info = existing.getInfo();
    if (existing.getInfo() != null
        && (request.getAddress() != null
            || request.getDepartment() != null
            || request.getCity() != null
            || request.getDniType() != null
            || request.getDniNumber() != null)) {
      info =
          new CustomerInfo(
              request.getDniType() != null
                  ? new CustomerDniType(request.getDniType())
                  : existing.getInfo().getDniType(),
              request.getDniNumber() != null
                  ? new CustomerDniNumber(request.getDniNumber())
                  : existing.getInfo().getDniNumber(),
              request.getAddress() != null
                  ? new CustomerAddress(request.getAddress())
                  : existing.getInfo().getAddress(),
              request.getDepartment() != null
                  ? new CustomerDepartment(request.getDepartment())
                  : existing.getInfo().getDepartment(),
              request.getCity() != null
                  ? new CustomerCity(request.getCity())
                  : existing.getInfo().getCity(),
              existing.getInfo().getDniCreatedAt(),
              existing.getInfo().getDniUpdatedAt());
    }

    Customer customer =
        new Customer(
            existing.getId(),
            request.getFirstName() != null
                ? new CustomerFirstName(request.getFirstName())
                : existing.getFirstName(),
            request.getLastName() != null
                ? new CustomerLastName(request.getLastName())
                : existing.getLastName(),
            existing.getEmail(),
            request.getPhoneNumber() != null
                ? new CustomerPhoneNumber(request.getPhoneNumber())
                : existing.getPhoneNumber(),
            existing.getPassword(),
            request.getProfileImage() != null
                ? new CustomerProfileImage(request.getProfileImage())
                : existing.getProfileImage(),
            existing.getCreatedAt(),
            existing.getUpdatedAt(),
            info);
    updateUseCase.execute(customer);
    Customer updatedCustomer = findByIdUseCase.execute(new CustomerId(id)).orElseThrow();
    return ResponseEntity.ok(mapper.toResponse(updatedCustomer));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    deleteUseCase.execute(new CustomerId(id));
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  public ResponseEntity<List<CustomerResponse>> findAll() {
    List<Customer> customers = findAllUseCase.execute();
    List<CustomerResponse> responses =
        customers.stream().map(mapper::toResponse).collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/{id}")
  public ResponseEntity<CustomerResponse> findById(@PathVariable UUID id) {
    return findByIdUseCase
        .execute(new CustomerId(id))
        .map(customer -> ResponseEntity.ok(mapper.toResponse(customer)))
        .orElse(ResponseEntity.notFound().build());
  }
}
