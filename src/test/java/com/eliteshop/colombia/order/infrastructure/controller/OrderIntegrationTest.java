package com.eliteshop.colombia.order.infrastructure.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.eliteshop.colombia.customer.domain.model.*;
import com.eliteshop.colombia.customer.domain.repository.CustomerRepository;
import com.eliteshop.colombia.order.domain.model.*;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import com.eliteshop.colombia.order.infrastructure.controller.dto.OrderRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS,
    statements = {
      "MERGE INTO department (id, name) KEY (id) VALUES (11, 'Bogotá D.C.')",
      "MERGE INTO department (id, name) KEY (id) VALUES (5, 'Antioquia')",
      "MERGE INTO city (id, name, department_id) KEY (id) VALUES (11001, 'Bogotá', 11)",
      "MERGE INTO city (id, name, department_id) KEY (id) VALUES (5001, 'Medellín', 5)"
    })
class OrderIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private CustomerRepository customerRepository;
  @Autowired private OrderRepository orderRepository;
  private final ObjectMapper objectMapper = new ObjectMapper();

  private UUID customerId;
  private UUID otherCustomerId;

  @BeforeEach
  void setUp() {
    customerId = UUID.randomUUID();
    otherCustomerId = UUID.randomUUID();
    saveCustomer(customerId, "owner@test.com");
    saveCustomer(otherCustomerId, "other@test.com");
  }

  @AfterEach
  void cleanUp() {
    customerRepository.delete(new CustomerId(customerId));
    customerRepository.delete(new CustomerId(otherCustomerId));
  }

  @Test
  @WithMockUser(username = "00000000-0000-0000-0000-000000000001", roles = "CUSTOMER")
  void shouldCreateOrder() throws Exception {
    OrderRequest request = buildOrderRequest(customerId);

    mockMvc
        .perform(
            post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.totalAmount").value(250000));
  }

  @Test
  @WithMockUser(username = "00000000-0000-0000-0000-000000000001", roles = "CUSTOMER")
  void shouldRejectCreateOrderWithoutRequiredFields() throws Exception {
    OrderRequest request = new OrderRequest();

    mockMvc
        .perform(
            post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(username = "00000000-0000-0000-0000-000000000001", roles = "CUSTOMER")
  void shouldGetOrderById() throws Exception {
    String createResponse =
        mockMvc
            .perform(
                post("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(buildOrderRequest(customerId))))
            .andReturn()
            .getResponse()
            .getContentAsString();

    UUID createdOrderId = UUID.fromString(objectMapper.readTree(createResponse).get("id").asText());

    mockMvc
        .perform(get("/api/v1/orders/{id}", createdOrderId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(createdOrderId.toString()));
  }

  @Test
  @WithMockUser(username = "00000000-0000-0000-0000-000000000001", roles = "CUSTOMER")
  void shouldReturn404ForNonExistentOrder() throws Exception {
    UUID nonExistentId = UUID.randomUUID();

    mockMvc.perform(get("/api/v1/orders/{id}", nonExistentId)).andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(username = "00000000-0000-0000-0000-000000000001", roles = "CUSTOMER")
  void shouldListOrdersByCustomerId() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildOrderRequest(customerId))))
        .andExpect(status().isCreated());

    mockMvc
        .perform(get("/api/v1/orders").param("page", "0").param("size", "25"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }

  @Test
  @WithMockUser(username = "00000000-0000-0000-0000-000000000001", roles = "CUSTOMER")
  void shouldCancelOrder() throws Exception {
    String createResponse =
        mockMvc
            .perform(
                post("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(buildOrderRequest(customerId))))
            .andReturn()
            .getResponse()
            .getContentAsString();

    UUID createdOrderId = UUID.fromString(objectMapper.readTree(createResponse).get("id").asText());

    mockMvc
        .perform(patch("/api/v1/orders/{id}/cancel", createdOrderId))
        .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(username = "00000000-0000-0000-0000-000000000001", roles = "CUSTOMER")
  void shouldRejectAccessToOtherCustomerOrder() throws Exception {
    UUID otherOrderId = UUID.randomUUID();
    orderRepository.save(
        new Order(
            new OrderId(otherOrderId),
            new OrderCustomerId(otherCustomerId),
            OrderStatus.PAID,
            new OrderTotalAmount(new BigDecimal("100000")),
            new OrderShippingAddress("Calle 99"),
            new OrderShippingDepartment("Medellin"),
            new OrderShippingCity("Medellin"),
            new OrderCreatedAt(Timestamp.from(Instant.now())),
            null,
            null,
            null,
            null,
            null));

    mockMvc.perform(get("/api/v1/orders/{id}", otherOrderId)).andExpect(status().isForbidden());
  }

  private OrderRequest buildOrderRequest(UUID custId) {
    OrderRequest request = new OrderRequest();
    request.setCustomerId(custId);
    request.setTotalAmount(new BigDecimal("250000"));
    request.setShippingAddress("Calle 100 #15-20");
    request.setShippingDepartment("Bogotá D.C.");
    request.setShippingCity("Bogotá");
    return request;
  }

  private void saveCustomer(UUID id, String email) {
    customerRepository.save(
        new Customer(
            new CustomerId(id),
            new CustomerFirstName("Test"),
            new CustomerLastName("User"),
            new CustomerEmail(email),
            new CustomerPhoneNumber("3001234567"),
            new CustomerPassword("password"),
            null,
            new CustomerCreatedAt(Timestamp.from(Instant.now())),
            null,
            null));
  }
}
