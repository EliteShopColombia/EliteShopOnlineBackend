package com.eliteshop.colombia.shared.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<DepartmentEntity, Integer> {

  Optional<DepartmentEntity> findByName(String name);
}
