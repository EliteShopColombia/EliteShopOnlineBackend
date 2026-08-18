package com.eliteshop.colombia.auth.infrastructure.mapper;

import com.eliteshop.colombia.auth.infrastructure.controller.dto.AuthResponse;
import com.eliteshop.colombia.auth.infrastructure.controller.dto.RegisterRequest;
import com.eliteshop.colombia.customer.domain.model.Customer;
import com.eliteshop.colombia.customer.domain.model.CustomerAddress;
import com.eliteshop.colombia.customer.domain.model.CustomerCity;
import com.eliteshop.colombia.customer.domain.model.CustomerCreatedAt;
import com.eliteshop.colombia.customer.domain.model.CustomerDepartment;
import com.eliteshop.colombia.customer.domain.model.CustomerDniCreatedAt;
import com.eliteshop.colombia.customer.domain.model.CustomerDniNumber;
import com.eliteshop.colombia.customer.domain.model.CustomerDniType;
import com.eliteshop.colombia.customer.domain.model.CustomerEmail;
import com.eliteshop.colombia.customer.domain.model.CustomerFirstName;
import com.eliteshop.colombia.customer.domain.model.CustomerId;
import com.eliteshop.colombia.customer.domain.model.CustomerInfo;
import com.eliteshop.colombia.customer.domain.model.CustomerLastName;
import com.eliteshop.colombia.customer.domain.model.CustomerPassword;
import com.eliteshop.colombia.customer.domain.model.CustomerPhoneNumber;
import com.eliteshop.colombia.customer.domain.model.CustomerProfileImage;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

  public Customer toDomainFromRegisterRequest(RegisterRequest request) {
    CustomerInfo info = null;
    if (request.getDniType() != null) {
      info =
          new CustomerInfo(
              new CustomerDniType(request.getDniType()),
              new CustomerDniNumber(request.getDniNumber()),
              new CustomerAddress(request.getAddress()),
              new CustomerDepartment(request.getDepartment()),
              new CustomerCity(request.getCity()),
              new CustomerDniCreatedAt(Timestamp.from(Instant.now())),
              null);
    }
    return new Customer(
        new CustomerId(UUID.randomUUID()),
        new CustomerFirstName(request.getFirstName()),
        new CustomerLastName(request.getLastName()),
        new CustomerEmail(request.getEmail()),
        new CustomerPhoneNumber(request.getPhoneNumber()),
        new CustomerPassword(request.getPassword()),
        request.getProfileImage() != null
            ? new CustomerProfileImage(request.getProfileImage())
            : null,
        new CustomerCreatedAt(Timestamp.from(Instant.now())),
        null,
        info);
  }

  public AuthResponse toAuthResponse(Customer customer, String token, long expiration) {
    return new AuthResponse(
        token,
        expiration,
        new AuthResponse.UserInfo(
            customer.getId().getValue(),
            customer.getEmail().getValue(),
            customer.getFirstName().getValue(),
            customer.getLastName().getValue(),
            "customer"));
  }
}
