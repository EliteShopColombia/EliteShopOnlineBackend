package com.eliteshop.colombia.shared.domain;

import com.eliteshop.colombia.shared.exception.InvalidLocationException;
import com.eliteshop.colombia.shared.infrastructure.persistence.CityRepository;
import com.eliteshop.colombia.shared.infrastructure.persistence.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Centralized validation service for Colombian departments and cities. Validates that location
 * names exist in the database tables seeded by Liquibase migrations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocationValidationService {

  private final DepartmentRepository departmentRepository;
  private final CityRepository cityRepository;

  /**
   * Validates that the department name exists in the database.
   *
   * @param departmentName the department name to validate (e.g., "Antioquia")
   * @throws InvalidLocationException if the department does not exist
   */
  public void validateDepartment(String departmentName) {
    if (departmentName == null || departmentName.isBlank()) {
      throw new InvalidLocationException("El departamento es requerido");
    }
    departmentRepository
        .findByName(departmentName.trim())
        .orElseThrow(
            () ->
                new InvalidLocationException(
                    "El departamento '"
                        + departmentName
                        + "' no es válido. "
                        + "Use uno de los 33 departamentos de Colombia."));
  }

  /**
   * Validates that the city belongs to the specified department.
   *
   * @param departmentName the department name (must exist)
   * @param cityName the city name to validate
   * @throws InvalidLocationException if the city or department does not exist, or if the city does
   *     not belong to the department
   */
  public void validateCity(String departmentName, String cityName) {
    if (cityName == null || cityName.isBlank()) {
      throw new InvalidLocationException("La ciudad es requerida");
    }
    var dept =
        departmentRepository
            .findByName(departmentName != null ? departmentName.trim() : "")
            .orElseThrow(
                () ->
                    new InvalidLocationException(
                        "No se puede validar la ciudad: el departamento '"
                            + departmentName
                            + "' no es válido"));

    cityRepository
        .findByNameAndDepartmentId(cityName.trim(), dept.getId())
        .orElseThrow(
            () ->
                new InvalidLocationException(
                    "La ciudad '"
                        + cityName
                        + "' no pertenece al departamento '"
                        + departmentName
                        + "'"));
  }

  /**
   * Validates both department and city in a single call.
   *
   * @param departmentName the department name
   * @param cityName the city name
   * @throws InvalidLocationException if either is invalid
   */
  public void validateLocation(String departmentName, String cityName) {
    validateDepartment(departmentName);
    validateCity(departmentName, cityName);
  }
}
