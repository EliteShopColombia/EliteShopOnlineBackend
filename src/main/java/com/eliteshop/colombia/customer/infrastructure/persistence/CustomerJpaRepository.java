package com.eliteshop.colombia.customer.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerJpaRepository extends JpaRepository<CustomerEntity, UUID> {
  Optional<CustomerEntity> findByEmail(String email);

  Page<CustomerEntity> findAll(Pageable pageable);
}
