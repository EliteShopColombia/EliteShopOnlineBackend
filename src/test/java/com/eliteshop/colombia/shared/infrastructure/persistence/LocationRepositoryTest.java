package com.eliteshop.colombia.shared.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Sql(
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS,
    statements = {
      "INSERT INTO department (id, name) VALUES (5, 'Antioquia')",
      "INSERT INTO department (id, name) VALUES (11, 'Bogotá D.C.')",
      "INSERT INTO department (id, name) VALUES (76, 'Valle del Cauca')",
      "INSERT INTO department (id, name) VALUES (88, 'Archipiélago de San Andrés, Providencia y Santa Catalina')",
      "INSERT INTO city (id, name, department_id) VALUES (5001, 'Medellín', 5)",
      "INSERT INTO city (id, name, department_id) VALUES (5088, 'Bello', 5)",
      "INSERT INTO city (id, name, department_id) VALUES (11001, 'Bogotá', 11)",
      "INSERT INTO city (id, name, department_id) VALUES (76001, 'Cali', 76)",
      "INSERT INTO city (id, name, department_id) VALUES (88001, 'San Andrés', 88)"
    })
class LocationRepositoryTest {

  @Autowired private DepartmentRepository departmentRepository;

  @Autowired private CityRepository cityRepository;

  // ========== DepartmentRepository ==========

  @Test
  void shouldFindDepartmentByName() {
    var result = departmentRepository.findByName("Antioquia");
    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(5);
    assertThat(result.get().getName()).isEqualTo("Antioquia");
  }

  @Test
  void shouldFindDepartmentWithLongName() {
    var result =
        departmentRepository.findByName("Archipiélago de San Andrés, Providencia y Santa Catalina");
    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(88);
  }

  @Test
  void shouldReturnEmptyForInvalidDepartment() {
    var result = departmentRepository.findByName("Putita");
    assertThat(result).isEmpty();
  }

  @Test
  void shouldReturnEmptyForNullDepartment() {
    var result = departmentRepository.findByName(null);
    assertThat(result).isEmpty();
  }

  @Test
  void shouldReturnEmptyForBlankDepartment() {
    var result = departmentRepository.findByName("   ");
    assertThat(result).isEmpty();
  }

  // ========== CityRepository ==========

  @Test
  void shouldFindCityByNameAndDepartmentId() {
    var result = cityRepository.findByNameAndDepartmentId("Medellín", 5);
    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(5001);
    assertThat(result.get().getName()).isEqualTo("Medellín");
    assertThat(result.get().getDepartmentId()).isEqualTo(5);
  }

  @Test
  void shouldFindCityInBogota() {
    var result = cityRepository.findByNameAndDepartmentId("Bogotá", 11);
    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(11001);
  }

  @Test
  void shouldReturnEmptyForCityInWrongDepartment() {
    // Medellín no está en Bogotá D.C.
    var result = cityRepository.findByNameAndDepartmentId("Medellín", 11);
    assertThat(result).isEmpty();
  }

  @Test
  void shouldReturnEmptyForInvalidCity() {
    var result = cityRepository.findByNameAndDepartmentId("Testiculo", 5);
    assertThat(result).isEmpty();
  }

  @Test
  void shouldReturnEmptyForNullCity() {
    var result = cityRepository.findByNameAndDepartmentId(null, 5);
    assertThat(result).isEmpty();
  }

  @Test
  void shouldReturnEmptyForNullDepartmentId() {
    var result = cityRepository.findByNameAndDepartmentId("Medellín", null);
    assertThat(result).isEmpty();
  }

  // ========== Cross-validation ==========

  @Test
  void shouldValidateDepartmentThenCity() {
    var department = departmentRepository.findByName("Antioquia");
    assertThat(department).isPresent();

    var city = cityRepository.findByNameAndDepartmentId("Medellín", department.get().getId());
    assertThat(city).isPresent();
  }

  @Test
  void shouldRejectCityWhenDepartmentMismatch() {
    var department = departmentRepository.findByName("Antioquia");
    assertThat(department).isPresent();

    var city = cityRepository.findByNameAndDepartmentId("Cali", department.get().getId());
    assertThat(city).isEmpty();
  }

  @Test
  void shouldFindAllDepartments() {
    var departments = departmentRepository.findAll();
    assertThat(departments).hasSize(4);
  }

  @Test
  void shouldFindAllCities() {
    var cities = cityRepository.findAll();
    assertThat(cities).hasSize(5);
  }
}
