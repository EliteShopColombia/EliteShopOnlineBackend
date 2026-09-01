package com.eliteshop.colombia.admin.infrastructure.mapper;

import com.eliteshop.colombia.admin.infrastructure.controller.dto.AdminRegisterRequest;
import com.eliteshop.colombia.admin.infrastructure.controller.dto.AdminRegisterResponse;
import com.eliteshop.colombia.customer.domain.model.Customer;
import com.eliteshop.colombia.customer.domain.model.CustomerCreatedAt;
import com.eliteshop.colombia.customer.domain.model.CustomerEmail;
import com.eliteshop.colombia.customer.domain.model.CustomerFirstName;
import com.eliteshop.colombia.customer.domain.model.CustomerId;
import com.eliteshop.colombia.customer.domain.model.CustomerLastName;
import com.eliteshop.colombia.customer.domain.model.CustomerPassword;
import com.eliteshop.colombia.customer.domain.model.CustomerPhoneNumber;
import com.eliteshop.colombia.customer.domain.model.CustomerRole;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AdminMapper {

  public Customer toDomainFromRequest(AdminRegisterRequest request) {
    return new Customer(
        new CustomerId(UUID.randomUUID()),
        new CustomerFirstName(request.getFirstName()),
        new CustomerLastName(request.getLastName()),
        new CustomerEmail(request.getEmail()),
        new CustomerPhoneNumber(request.getPhoneNumber()),
        new CustomerPassword(request.getPassword()),
        null,
        new CustomerRole("admin"),
        new CustomerCreatedAt(Timestamp.from(Instant.now())),
        null,
        null);
  }

  public AdminRegisterResponse toResponse(Customer customer) {
    return new AdminRegisterResponse(
        customer.getId().getValue(),
        customer.getEmail().getValue(),
        customer.getFirstName().getValue(),
        customer.getLastName().getValue(),
        customer.getRole().getValue());
  }
}
