package com.eliteshop.colombia.customer.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CustomerJpaRepository extends JpaRepository<CustomerEntity, UUID> {
  Optional<CustomerEntity> findByEmail(String email);

  @EntityGraph(attributePaths = {"info"})
  @Query("SELECT c FROM CustomerEntity c")
  Page<CustomerEntity> findAllWithInfo(Pageable pageable);
}
