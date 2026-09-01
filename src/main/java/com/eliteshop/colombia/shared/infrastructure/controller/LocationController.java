package com.eliteshop.colombia.shared.infrastructure.controller;

import com.eliteshop.colombia.shared.infrastructure.persistence.CityEntity;
import com.eliteshop.colombia.shared.infrastructure.persistence.CityRepository;
import com.eliteshop.colombia.shared.infrastructure.persistence.DepartmentEntity;
import com.eliteshop.colombia.shared.infrastructure.persistence.DepartmentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
public class LocationController {

  private final DepartmentRepository departmentRepository;
  private final CityRepository cityRepository;

  @GetMapping("/departments")
  public List<DepartmentEntity> getDepartments() {
    return departmentRepository.findAll(Sort.by("name"));
  }

  @GetMapping("/departments/{id}/cities")
  public List<CityEntity> getCitiesByDepartment(@PathVariable Integer id) {
    return cityRepository.findByDepartmentId(id, Sort.by("name"));
  }
}
