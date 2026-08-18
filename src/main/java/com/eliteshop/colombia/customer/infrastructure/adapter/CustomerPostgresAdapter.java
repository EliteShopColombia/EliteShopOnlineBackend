package com.eliteshop.colombia.customer.infrastructure.adapter;

import com.eliteshop.colombia.customer.domain.model.Customer;
import com.eliteshop.colombia.customer.domain.model.CustomerId;
import com.eliteshop.colombia.customer.domain.repository.CustomerRepository;
import com.eliteshop.colombia.customer.infrastructure.mapper.CustomerMapper;
import com.eliteshop.colombia.customer.infrastructure.persistence.CustomerEntity;
import com.eliteshop.colombia.customer.infrastructure.persistence.CustomerJpaRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerPostgresAdapter implements CustomerRepository {

  private final CustomerJpaRepository jpaRepository;
  private final CustomerMapper mapper;

  @Override
  public Customer save(Customer customer) {
    CustomerEntity entity = mapper.toEntity(customer);
    CustomerEntity savedEntity = jpaRepository.save(entity);
    return mapper.toDomain(savedEntity);
  }

  @Override
  public void update(Customer customer) {
    CustomerEntity existingEntity =
        jpaRepository.findById(customer.getId().getValue()).orElseThrow();
    existingEntity.setFirstName(customer.getFirstName().getValue());
    existingEntity.setLastName(customer.getLastName().getValue());
    existingEntity.setEmail(customer.getEmail().getValue());
    existingEntity.setPhoneNumber(customer.getPhoneNumber().getValue());
    existingEntity.setPassword(customer.getPassword().getValue());
    if (customer.getProfileImage() != null) {
      existingEntity.setProfileImage(customer.getProfileImage().getValue());
    }
    existingEntity.setUpdatedAt(Timestamp.from(Instant.now()));
    if (customer.getInfo() != null && existingEntity.getInfo() != null) {
      existingEntity.getInfo().setDniType(customer.getInfo().getDniType().getValue());
      existingEntity.getInfo().setDniNumber(customer.getInfo().getDniNumber().getValue());
      existingEntity.getInfo().setAddress(customer.getInfo().getAddress().getValue());
      existingEntity.getInfo().setDepartment(customer.getInfo().getDepartment().getValue());
      existingEntity.getInfo().setCity(customer.getInfo().getCity().getValue());
      existingEntity.getInfo().setDniUpdatedAt(Timestamp.from(Instant.now()));
    }
    jpaRepository.save(existingEntity);
  }

  @Override
  public void delete(CustomerId id) {
    jpaRepository.deleteById(id.getValue());
  }

  @Override
  public List<Customer> findAll() {
    return jpaRepository.findAll().stream().map(mapper::toDomain).collect(Collectors.toList());
  }

  @Override
  public Optional<Customer> findById(CustomerId id) {
    return jpaRepository.findById(id.getValue()).map(mapper::toDomain);
  }

  @Override
  public Optional<Customer> findByEmail(String email) {
    return jpaRepository.findByEmail(email).map(mapper::toDomain);
  }
}
