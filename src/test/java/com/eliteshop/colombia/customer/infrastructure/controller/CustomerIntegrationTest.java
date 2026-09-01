package com.eliteshop.colombia.customer.infrastructure.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.eliteshop.colombia.customer.domain.model.*;
import com.eliteshop.colombia.customer.domain.repository.CustomerRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CustomerIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private CustomerRepository customerRepository;

  private UUID customerId;

  @BeforeEach
  void setUp() {
    customerId = UUID.randomUUID();
    customerRepository.save(
        new Customer(
            new CustomerId(customerId),
            new CustomerFirstName("Juan"),
            new CustomerLastName("Perez"),
            new CustomerEmail("juan-" + customerId + "@test.com"),
            new CustomerPhoneNumber("3001234567"),
            new CustomerPassword("password"),
            null,
            new CustomerRole("customer"),
            new CustomerCreatedAt(Timestamp.from(Instant.now())),
            null,
            null));
  }

  @AfterEach
  void cleanUp() {
    customerRepository.delete(new CustomerId(customerId));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void shouldListAllCustomers() throws Exception {
    mockMvc.perform(get("/api/v1/customers")).andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void shouldRejectAdminAccessToCustomerById() throws Exception {
    mockMvc.perform(get("/api/v1/customers/{id}", customerId)).andExpect(status().isForbidden());
  }

  @Test
  void shouldRejectUnauthenticatedAccessToCustomer() throws Exception {
    mockMvc.perform(get("/api/v1/customers/{id}", customerId)).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "00000000-0000-0000-0000-000000000001", roles = "CUSTOMER")
  void shouldRejectAccessToOtherCustomer() throws Exception {
    mockMvc.perform(get("/api/v1/customers/{id}", customerId)).andExpect(status().isForbidden());
  }
}
