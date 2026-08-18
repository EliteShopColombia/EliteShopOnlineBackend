package com.eliteshop.colombia.customer.infrastructure.mapper;

import com.eliteshop.colombia.customer.domain.model.Customer;
import com.eliteshop.colombia.customer.domain.model.CustomerAddress;
import com.eliteshop.colombia.customer.domain.model.CustomerCity;
import com.eliteshop.colombia.customer.domain.model.CustomerCreatedAt;
import com.eliteshop.colombia.customer.domain.model.CustomerDepartment;
import com.eliteshop.colombia.customer.domain.model.CustomerDniCreatedAt;
import com.eliteshop.colombia.customer.domain.model.CustomerDniNumber;
import com.eliteshop.colombia.customer.domain.model.CustomerDniType;
import com.eliteshop.colombia.customer.domain.model.CustomerDniUpdatedAt;
import com.eliteshop.colombia.customer.domain.model.CustomerEmail;
import com.eliteshop.colombia.customer.domain.model.CustomerFirstName;
import com.eliteshop.colombia.customer.domain.model.CustomerId;
import com.eliteshop.colombia.customer.domain.model.CustomerInfo;
import com.eliteshop.colombia.customer.domain.model.CustomerLastName;
import com.eliteshop.colombia.customer.domain.model.CustomerPassword;
import com.eliteshop.colombia.customer.domain.model.CustomerPhoneNumber;
import com.eliteshop.colombia.customer.domain.model.CustomerProfileImage;
import com.eliteshop.colombia.customer.domain.model.CustomerUpdatedAt;
import com.eliteshop.colombia.customer.infrastructure.controller.dto.CustomerRequest;
import com.eliteshop.colombia.customer.infrastructure.controller.dto.CustomerResponse;
import com.eliteshop.colombia.customer.infrastructure.controller.dto.UpdateCustomerRequest;
import com.eliteshop.colombia.customer.infrastructure.persistence.CustomerEntity;
import com.eliteshop.colombia.customer.infrastructure.persistence.CustomerInfoEntity;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

  public Customer toDomain(CustomerEntity entity) {
    if (entity == null) {
      return null;
    }
    CustomerInfo info = null;
    if (entity.getInfo() != null) {
      info =
          new CustomerInfo(
              new CustomerDniType(entity.getInfo().getDniType()),
              new CustomerDniNumber(entity.getInfo().getDniNumber()),
              new CustomerAddress(entity.getInfo().getAddress()),
              new CustomerDepartment(entity.getInfo().getDepartment()),
              new CustomerCity(entity.getInfo().getCity()),
              new CustomerDniCreatedAt(entity.getInfo().getDniCreatedAt()),
              entity.getInfo().getDniUpdatedAt() != null
                  ? new CustomerDniUpdatedAt(entity.getInfo().getDniUpdatedAt())
                  : null);
    }
    return new Customer(
        new CustomerId(entity.getId()),
        new CustomerFirstName(entity.getFirstName()),
        new CustomerLastName(entity.getLastName()),
        new CustomerEmail(entity.getEmail()),
        new CustomerPhoneNumber(entity.getPhoneNumber()),
        new CustomerPassword(entity.getPassword()),
        entity.getProfileImage() != null
            ? new CustomerProfileImage(entity.getProfileImage())
            : null,
        new CustomerCreatedAt(entity.getCreatedAt()),
        entity.getUpdatedAt() != null ? new CustomerUpdatedAt(entity.getUpdatedAt()) : null,
        info);
  }

  public CustomerEntity toEntity(Customer domain) {
    if (domain == null) {
      return null;
    }
    CustomerEntity entity = new CustomerEntity();
    entity.setId(domain.getId().getValue());
    entity.setFirstName(domain.getFirstName().getValue());
    entity.setLastName(domain.getLastName().getValue());
    entity.setEmail(domain.getEmail().getValue());
    entity.setPhoneNumber(domain.getPhoneNumber().getValue());
    entity.setPassword(domain.getPassword().getValue());
    if (domain.getProfileImage() != null) {
      entity.setProfileImage(domain.getProfileImage().getValue());
    }
    entity.setCreatedAt(domain.getCreatedAt().getValue());
    if (domain.getUpdatedAt() != null) {
      entity.setUpdatedAt(domain.getUpdatedAt().getValue());
    }
    if (domain.getInfo() != null) {
      CustomerInfoEntity infoEntity = new CustomerInfoEntity();
      infoEntity.setCustomer(entity);
      infoEntity.setDniType(domain.getInfo().getDniType().getValue());
      infoEntity.setDniNumber(domain.getInfo().getDniNumber().getValue());
      infoEntity.setAddress(domain.getInfo().getAddress().getValue());
      infoEntity.setDepartment(domain.getInfo().getDepartment().getValue());
      infoEntity.setCity(domain.getInfo().getCity().getValue());
      infoEntity.setDniCreatedAt(domain.getInfo().getDniCreatedAt().getValue());
      if (domain.getInfo().getDniUpdatedAt() != null) {
        infoEntity.setDniUpdatedAt(domain.getInfo().getDniUpdatedAt().getValue());
      }
      entity.setInfo(infoEntity);
    }
    return entity;
  }

  public Customer toDomainFromRequest(CustomerRequest request) {
    if (request == null) {
      return null;
    }
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

  public Customer toDomainFromUpdateRequest(UUID id, UpdateCustomerRequest request) {
    if (request == null) {
      return null;
    }
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
        new CustomerId(id),
        new CustomerFirstName(request.getFirstName()),
        new CustomerLastName(request.getLastName()),
        null,
        new CustomerPhoneNumber(request.getPhoneNumber()),
        null,
        request.getProfileImage() != null
            ? new CustomerProfileImage(request.getProfileImage())
            : null,
        null,
        null,
        info);
  }

  public CustomerResponse toResponse(Customer domain) {
    if (domain == null) {
      return null;
    }
    CustomerResponse response = new CustomerResponse();
    response.setId(domain.getId().getValue());
    response.setFirstName(domain.getFirstName().getValue());
    response.setLastName(domain.getLastName().getValue());
    response.setEmail(domain.getEmail().getValue());
    response.setPhoneNumber(domain.getPhoneNumber().getValue());
    if (domain.getProfileImage() != null) {
      response.setProfileImage(domain.getProfileImage().getValue());
    }
    response.setCreatedAt(domain.getCreatedAt().getValue());
    if (domain.getUpdatedAt() != null) {
      response.setUpdatedAt(domain.getUpdatedAt().getValue());
    }
    if (domain.getInfo() != null) {
      response.setDniType(domain.getInfo().getDniType().getValue());
      response.setDniNumber(domain.getInfo().getDniNumber().getValue());
      response.setAddress(domain.getInfo().getAddress().getValue());
      response.setDepartment(domain.getInfo().getDepartment().getValue());
      response.setCity(domain.getInfo().getCity().getValue());
      response.setDniCreatedAt(domain.getInfo().getDniCreatedAt().getValue());
      if (domain.getInfo().getDniUpdatedAt() != null) {
        response.setDniUpdatedAt(domain.getInfo().getDniUpdatedAt().getValue());
      }
    }
    return response;
  }
}
