package com.eliteshop.colombia.shared.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CityRepository extends JpaRepository<CityEntity, Integer> {

  List<CityEntity> findByDepartmentId(Integer departmentId, Sort sort);

  Optional<CityEntity> findByNameAndDepartmentId(String name, Integer departmentId);
}
