package com.eliteshop.colombia.shared.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CityRepository extends JpaRepository<CityEntity, Integer> {

  Optional<CityEntity> findByNameAndDepartmentId(String name, Integer departmentId);
}
