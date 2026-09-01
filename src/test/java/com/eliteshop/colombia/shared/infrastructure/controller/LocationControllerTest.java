package com.eliteshop.colombia.shared.infrastructure.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eliteshop.colombia.shared.infrastructure.persistence.CityEntity;
import com.eliteshop.colombia.shared.infrastructure.persistence.CityRepository;
import com.eliteshop.colombia.shared.infrastructure.persistence.DepartmentEntity;
import com.eliteshop.colombia.shared.infrastructure.persistence.DepartmentRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class LocationControllerTest {

  private MockMvc mockMvc;
  private DepartmentRepository departmentRepository;
  private CityRepository cityRepository;

  @BeforeEach
  void setUp() {
    departmentRepository = org.mockito.Mockito.mock(DepartmentRepository.class);
    cityRepository = org.mockito.Mockito.mock(CityRepository.class);

    LocationController controller = new LocationController(departmentRepository, cityRepository);

    mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  @Test
  void shouldReturnAllDepartments() throws Exception {
    DepartmentEntity dept1 = new DepartmentEntity();
    dept1.setId(5);
    dept1.setName("Antioquia");

    DepartmentEntity dept2 = new DepartmentEntity();
    dept2.setId(11);
    dept2.setName("Bogota D.C.");

    when(departmentRepository.findAll(Sort.by("name"))).thenReturn(List.of(dept1, dept2));

    mockMvc
        .perform(get("/api/v1/locations/departments"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(5))
        .andExpect(jsonPath("$[0].name").value("Antioquia"))
        .andExpect(jsonPath("$[1].id").value(11))
        .andExpect(jsonPath("$[1].name").value("Bogota D.C."));
  }

  @Test
  void shouldReturnEmptyListWhenNoDepartments() throws Exception {
    when(departmentRepository.findAll(Sort.by("name"))).thenReturn(List.of());

    mockMvc
        .perform(get("/api/v1/locations/departments"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void shouldReturnCitiesForDepartment() throws Exception {
    CityEntity city1 = new CityEntity();
    city1.setId(5001);
    city1.setName("Medellin");
    city1.setDepartmentId(5);

    CityEntity city2 = new CityEntity();
    city2.setId(5088);
    city2.setName("Bello");
    city2.setDepartmentId(5);

    when(cityRepository.findByDepartmentId(5, Sort.by("name"))).thenReturn(List.of(city1, city2));

    mockMvc
        .perform(get("/api/v1/locations/departments/5/cities"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(5001))
        .andExpect(jsonPath("$[0].name").value("Medellin"))
        .andExpect(jsonPath("$[0].departmentId").value(5))
        .andExpect(jsonPath("$[1].id").value(5088))
        .andExpect(jsonPath("$[1].name").value("Bello"));
  }

  @Test
  void shouldReturnEmptyListWhenDepartmentHasNoCities() throws Exception {
    when(cityRepository.findByDepartmentId(999, Sort.by("name"))).thenReturn(List.of());

    mockMvc
        .perform(get("/api/v1/locations/departments/999/cities"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));
  }
}
