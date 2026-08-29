package com.eliteshop.colombia.auth.infrastructure.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.eliteshop.colombia.auth.infrastructure.controller.dto.LoginRequest;
import com.eliteshop.colombia.auth.infrastructure.controller.dto.RegisterRequest;
import com.eliteshop.colombia.customer.domain.model.*;
import com.eliteshop.colombia.customer.domain.repository.CustomerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private CustomerRepository customerRepository;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void shouldRegisterCustomer() throws Exception {
    String email = "new-" + UUID.randomUUID() + "@test.com";
    RegisterRequest request = buildRegisterRequest(email);

    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.token").isNotEmpty())
        .andExpect(jsonPath("$.user.email").value(email))
        .andExpect(jsonPath("$.user.role").value("customer"));

    cleanupCustomer(email);
  }

  @Test
  void shouldRejectRegisterWithInvalidEmail() throws Exception {
    RegisterRequest request = buildRegisterRequest("not-an-email");

    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldRejectRegisterWithShortPassword() throws Exception {
    RegisterRequest request = buildRegisterRequest("short-" + UUID.randomUUID() + "@test.com");
    request.setPassword("123");

    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldLoginAfterRegister() throws Exception {
    String email = "login-" + UUID.randomUUID() + "@test.com";
    RegisterRequest regRequest = buildRegisterRequest(email);
    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regRequest)))
        .andExpect(status().isCreated());

    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setEmail(email);
    loginRequest.setPassword("password123");

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").isNotEmpty())
        .andExpect(jsonPath("$.user.email").value(email));

    cleanupCustomer(email);
  }

  @Test
  void shouldRejectLoginWithWrongPassword() throws Exception {
    String email = "wrong-" + UUID.randomUUID() + "@test.com";
    RegisterRequest regRequest = buildRegisterRequest(email);
    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regRequest)))
        .andExpect(status().isCreated());

    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setEmail(email);
    loginRequest.setPassword("wrongpassword");

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isUnauthorized());

    cleanupCustomer(email);
  }

  @Test
  void shouldRejectLoginWithNonExistentEmail() throws Exception {
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setEmail("nonexistent-" + UUID.randomUUID() + "@test.com");
    loginRequest.setPassword("password123");

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void shouldRefreshToken() throws Exception {
    String email = "refresh-" + UUID.randomUUID() + "@test.com";
    RegisterRequest regRequest = buildRegisterRequest(email);
    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(regRequest)))
            .andExpect(status().isCreated())
            .andReturn();

    String token =
        objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();

    mockMvc
        .perform(post("/api/v1/auth/refresh").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").isNotEmpty())
        .andExpect(jsonPath("$.user.email").value(email));

    cleanupCustomer(email);
  }

  @Test
  void shouldRejectRefreshWithInvalidToken() throws Exception {
    mockMvc
        .perform(post("/api/v1/auth/refresh").header("Authorization", "Bearer invalid-token-here"))
        .andExpect(status().isUnauthorized());
  }

  private RegisterRequest buildRegisterRequest(String email) {
    RegisterRequest request = new RegisterRequest();
    request.setFirstName("Test");
    request.setLastName("User");
    request.setEmail(email);
    request.setPhoneNumber("3001234567");
    request.setPassword("password123");
    request.setDniType("CC");
    request.setDniNumber(
        String.valueOf(Math.abs(UUID.randomUUID().hashCode()) % 900000000 + 100000000));
    request.setAddress("Calle 100");
    request.setDepartment("Bogota");
    request.setCity("Bogota D.C.");
    return request;
  }

  private void cleanupCustomer(String email) {
    customerRepository
        .findByEmail(email)
        .ifPresent(c -> customerRepository.delete(new CustomerId(c.getId().getValue())));
  }
}
