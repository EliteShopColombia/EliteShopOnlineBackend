package com.eliteshop.colombia.customer.infrastructure.controller;

import com.eliteshop.colombia.customer.application.CustomerDeleteUseCase;
import com.eliteshop.colombia.customer.application.CustomerFindAllUseCase;
import com.eliteshop.colombia.customer.application.CustomerFindByIdUseCase;
import com.eliteshop.colombia.customer.application.CustomerSaveUseCase;
import com.eliteshop.colombia.customer.application.CustomerUpdateUseCase;
import com.eliteshop.colombia.customer.domain.model.Customer;
import com.eliteshop.colombia.customer.domain.model.CustomerId;
import com.eliteshop.colombia.customer.infrastructure.controller.dto.CustomerRequest;
import com.eliteshop.colombia.customer.infrastructure.controller.dto.CustomerResponse;
import com.eliteshop.colombia.customer.infrastructure.mapper.CustomerMapper;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

  private final CustomerSaveUseCase saveUseCase;
  private final CustomerUpdateUseCase updateUseCase;
  private final CustomerDeleteUseCase deleteUseCase;
  private final CustomerFindAllUseCase findAllUseCase;
  private final CustomerFindByIdUseCase findByIdUseCase;
  private final CustomerMapper mapper;

  @PostMapping
  public ResponseEntity<CustomerResponse> save(@Valid @RequestBody CustomerRequest request) {
    Customer customer = mapper.toDomainFromRequest(request);
    saveUseCase.execute(customer);
    return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(customer));
  }

  @PutMapping("/{id}")
  public ResponseEntity<CustomerResponse> update(
      @PathVariable UUID id, @Valid @RequestBody CustomerRequest request) {
    Customer customer = mapper.toDomainFromRequest(request);
    customer =
        new Customer(
            new CustomerId(id),
            customer.getFirstName(),
            customer.getLastName(),
            customer.getEmail(),
            customer.getPhoneNumber(),
            customer.getPassword(),
            customer.getProfileImage(),
            customer.getCreatedAt(),
            customer.getUpdatedAt(),
            customer.getInfo());
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
