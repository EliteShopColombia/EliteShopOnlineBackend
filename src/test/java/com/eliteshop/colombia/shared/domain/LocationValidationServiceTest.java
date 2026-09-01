package com.eliteshop.colombia.shared.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.eliteshop.colombia.shared.exception.InvalidLocationException;
import com.eliteshop.colombia.shared.infrastructure.persistence.CityRepository;
import com.eliteshop.colombia.shared.infrastructure.persistence.DepartmentEntity;
import com.eliteshop.colombia.shared.infrastructure.persistence.DepartmentRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LocationValidationServiceTest {

  @Mock private DepartmentRepository departmentRepository;

  @Mock private CityRepository cityRepository;

  @InjectMocks private LocationValidationService service;

  // ========== validateDepartment ==========

  @Test
  void shouldPassWhenDepartmentIsValid() {
    when(departmentRepository.findByName("Antioquia"))
        .thenReturn(Optional.of(buildDepartment(5, "Antioquia")));

    service.validateDepartment("Antioquia");
    // No exception thrown = pass
  }

  @Test
  void shouldRejectWhenDepartmentIsInvalid() {
    when(departmentRepository.findByName("Putita")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.validateDepartment("Putita"))
        .isInstanceOf(InvalidLocationException.class)
        .hasMessageContaining("Putita")
        .hasMessageContaining("no es válido");
  }

  @Test
  void shouldRejectWhenDepartmentIsNull() {
    assertThatThrownBy(() -> service.validateDepartment(null))
        .isInstanceOf(InvalidLocationException.class)
        .hasMessageContaining("requerido");
  }

  @Test
  void shouldRejectWhenDepartmentIsBlank() {
    assertThatThrownBy(() -> service.validateDepartment("   "))
        .isInstanceOf(InvalidLocationException.class)
        .hasMessageContaining("requerido");
  }

  @Test
  void shouldTrimDepartmentName() {
    when(departmentRepository.findByName("Antioquia"))
        .thenReturn(Optional.of(buildDepartment(5, "Antioquia")));

    service.validateDepartment("  Antioquia  ");
    // No exception = pass, and the repository was called with trimmed value
  }

  // ========== validateCity ==========

  @Test
  void shouldPassWhenCityBelongsToDepartment() {
    when(departmentRepository.findByName("Antioquia"))
        .thenReturn(Optional.of(buildDepartment(5, "Antioquia")));
    when(cityRepository.findByNameAndDepartmentId("Medellín", 5))
        .thenReturn(Optional.of(buildCity(5001, "Medellín", 5)));

    service.validateCity("Antioquia", "Medellín");
  }

  @Test
  void shouldRejectWhenCityDoesNotBelongToDepartment() {
    when(departmentRepository.findByName("Antioquia"))
        .thenReturn(Optional.of(buildDepartment(5, "Antioquia")));
    when(cityRepository.findByNameAndDepartmentId("Cali", 5)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.validateCity("Antioquia", "Cali"))
        .isInstanceOf(InvalidLocationException.class)
        .hasMessageContaining("Cali")
        .hasMessageContaining("no pertenece");
  }

  @Test
  void shouldRejectWhenCityIsInvalid() {
    when(departmentRepository.findByName("Antioquia"))
        .thenReturn(Optional.of(buildDepartment(5, "Antioquia")));
    when(cityRepository.findByNameAndDepartmentId("Testiculo", 5)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.validateCity("Antioquia", "Testiculo"))
        .isInstanceOf(InvalidLocationException.class)
        .hasMessageContaining("Testiculo");
  }

  @Test
  void shouldRejectWhenCityIsNull() {
    assertThatThrownBy(() -> service.validateCity("Antioquia", null))
        .isInstanceOf(InvalidLocationException.class)
        .hasMessageContaining("requerida");
  }

  @Test
  void shouldRejectWhenCityIsBlank() {
    assertThatThrownBy(() -> service.validateCity("Antioquia", "   "))
        .isInstanceOf(InvalidLocationException.class)
        .hasMessageContaining("requerida");
  }

  @Test
  void shouldRejectWhenDepartmentIsInvalidForCityValidation() {
    when(departmentRepository.findByName("FakeDept")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.validateCity("FakeDept", "AnyCity"))
        .isInstanceOf(InvalidLocationException.class)
        .hasMessageContaining("no es válido");
  }

  // ========== validateLocation ==========

  @Test
  void shouldPassWhenBothDepartmentAndCityAreValid() {
    when(departmentRepository.findByName("Bogotá D.C."))
        .thenReturn(Optional.of(buildDepartment(11, "Bogotá D.C.")));
    when(cityRepository.findByNameAndDepartmentId("Bogotá", 11))
        .thenReturn(Optional.of(buildCity(11001, "Bogotá", 11)));

    service.validateLocation("Bogotá D.C.", "Bogotá");
  }

  @Test
  void shouldRejectWhenDepartmentFailsInCombinedValidation() {
    when(departmentRepository.findByName("Putita")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.validateLocation("Putita", "AnyCity"))
        .isInstanceOf(InvalidLocationException.class)
        .hasMessageContaining("Putita");
  }

  @Test
  void shouldRejectWhenCityFailsInCombinedValidation() {
    when(departmentRepository.findByName("Antioquia"))
        .thenReturn(Optional.of(buildDepartment(5, "Antioquia")));
    when(cityRepository.findByNameAndDepartmentId("FalsaCiudad", 5)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.validateLocation("Antioquia", "FalsaCiudad"))
        .isInstanceOf(InvalidLocationException.class)
        .hasMessageContaining("FalsaCiudad");
  }

  // ========== Helpers ==========

  private DepartmentEntity buildDepartment(Integer id, String name) {
    var dept = new DepartmentEntity();
    dept.setId(id);
    dept.setName(name);
    return dept;
  }

  private com.eliteshop.colombia.shared.infrastructure.persistence.CityEntity buildCity(
      Integer id, String name, Integer departmentId) {
    var city = new com.eliteshop.colombia.shared.infrastructure.persistence.CityEntity();
    city.setId(id);
    city.setName(name);
    city.setDepartmentId(departmentId);
    return city;
  }
}
