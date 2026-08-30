package com.eliteshop.colombia.admin.infrastructure.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.eliteshop.colombia.customer.infrastructure.persistence.CustomerEntity;
import com.eliteshop.colombia.customer.infrastructure.persistence.CustomerJpaRepository;
import com.eliteshop.colombia.seller.infrastructure.persistence.SellerEntity;
import com.eliteshop.colombia.seller.infrastructure.persistence.SellerJpaRepository;
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
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private CustomerJpaRepository customerJpaRepository;
  @Autowired private SellerJpaRepository sellerJpaRepository;

  private UUID sellerId;

  @BeforeEach
  void setUp() {
    CustomerEntity customer = new CustomerEntity();
    customer.setId(UUID.randomUUID());
    customer.setFirstName("Admin");
    customer.setLastName("Test");
    customer.setEmail("admin-test-" + UUID.randomUUID() + "@test.com");
    customer.setPhoneNumber("3001234567");
    customer.setPassword("hashed");
    customer.setRole("admin");
    customer.setCreatedAt(Timestamp.from(Instant.now()));
    customerJpaRepository.save(customer);

    SellerEntity seller = new SellerEntity();
    sellerId = UUID.randomUUID();
    seller.setId(sellerId);
    seller.setTypeTrade("NORMAL");
    seller.setTypeDni("CC");
    seller.setDniNumber("12345" + UUID.randomUUID().toString().substring(0, 5));
    seller.setTradeName("Test Store");
    seller.setFullname("Test Seller");
    seller.setIsActive(true);
    seller.setIsVerified(false);
    seller.setCreatedAt(Timestamp.from(Instant.now()));
    sellerJpaRepository.save(seller);
  }

  @AfterEach
  void cleanUp() {
    sellerJpaRepository.deleteAll();
    customerJpaRepository.deleteAll();
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void getDashboard_shouldReturnStats() throws Exception {
    mockMvc
        .perform(get("/api/v1/admin/dashboard"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalCustomers").isNumber())
        .andExpect(jsonPath("$.totalSellers").isNumber())
        .andExpect(jsonPath("$.activeSellers").isNumber())
        .andExpect(jsonPath("$.totalOrders").isNumber())
        .andExpect(jsonPath("$.totalRevenue").isNumber())
        .andExpect(jsonPath("$.totalProducts").isNumber());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void getAllCustomers_shouldReturnPaginatedList() throws Exception {
    mockMvc
        .perform(get("/api/v1/admin/customers").param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.totalElements").isNumber());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void getAllSellers_shouldReturnPaginatedList() throws Exception {
    mockMvc
        .perform(get("/api/v1/admin/sellers").param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.totalElements").isNumber());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void getSellerById_shouldReturnSeller() throws Exception {
    mockMvc
        .perform(get("/api/v1/admin/sellers/" + sellerId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(sellerId.toString()));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void getSellerById_shouldReturn404_whenNotFound() throws Exception {
    mockMvc
        .perform(get("/api/v1/admin/sellers/" + UUID.randomUUID()))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void updateSellerStatus_shouldDeactivateSeller() throws Exception {
    mockMvc
        .perform(
            patch("/api/v1/admin/sellers/" + sellerId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"isActive\":false}"))
        .andExpect(status().isNoContent());

    SellerEntity updated = sellerJpaRepository.findById(sellerId).orElseThrow();
    assert !updated.getIsActive();
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void updateSellerStatus_shouldReturn404_whenNotFound() throws Exception {
    mockMvc
        .perform(
            patch("/api/v1/admin/sellers/" + UUID.randomUUID() + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"isActive\":false}"))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(roles = "CUSTOMER")
  void adminEndpoints_shouldReturn403_forCustomer() throws Exception {
    mockMvc.perform(get("/api/v1/admin/dashboard")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "SELLER")
  void adminEndpoints_shouldReturn403_forSeller() throws Exception {
    mockMvc.perform(get("/api/v1/admin/dashboard")).andExpect(status().isForbidden());
  }

  @Test
  void adminEndpoints_shouldReturn403_whenNotAuthenticated() throws Exception {
    mockMvc.perform(get("/api/v1/admin/dashboard")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void getAllOrders_shouldReturnPaginatedList() throws Exception {
    mockMvc
        .perform(get("/api/v1/admin/orders").param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void registerAdmin_shouldCreateAdmin_whenValidRequest() throws Exception {
    String email = "new-admin-" + UUID.randomUUID() + "@test.com";
    mockMvc
        .perform(
            post("/api/v1/admin/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{"
                        + "\"firstName\": \"New\","
                        + "\"lastName\": \"Admin\","
                        + "\"email\": \""
                        + email
                        + "\","
                        + "\"phoneNumber\": \"3001112233\","
                        + "\"password\": \"SecurePass123!\""
                        + "}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNotEmpty())
        .andExpect(jsonPath("$.email").isNotEmpty())
        .andExpect(jsonPath("$.firstName").value("New"))
        .andExpect(jsonPath("$.lastName").value("Admin"))
        .andExpect(jsonPath("$.role").value("admin"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void registerAdmin_shouldReturn409_whenEmailExists() throws Exception {
    String existingEmail = "existing-admin-" + UUID.randomUUID() + "@test.com";
    String body =
        "{"
            + "\"firstName\": \"Existing\","
            + "\"lastName\": \"Admin\","
            + "\"email\": \""
            + existingEmail
            + "\","
            + "\"phoneNumber\": \"3001112233\","
            + "\"password\": \"SecurePass123!\""
            + "}";

    // Create admin first
    mockMvc
        .perform(
            post("/api/v1/admin/register").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated());

    // Try to create again with same email
    mockMvc
        .perform(
            post("/api/v1/admin/register").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isConflict());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void registerAdmin_shouldReturn400_whenInvalidData() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/admin/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "firstName": "",
                      "lastName": "",
                      "email": "invalid-email",
                      "phoneNumber": "",
                      "password": "short"
                    }
                    """))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(roles = "CUSTOMER")
  void registerAdmin_shouldReturn403_forCustomer() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/admin/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "firstName": "Test",
                      "lastName": "User",
                      "email": "test@test.com",
                      "phoneNumber": "3001234567",
                      "password": "SecurePass123!"
                    }
                    """))
        .andExpect(status().isForbidden());
  }

  @Test
  void registerAdmin_shouldReturn403_whenNotAuthenticated() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/admin/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "firstName": "Test",
                      "lastName": "User",
                      "email": "test@test.com",
                      "phoneNumber": "3001234567",
                      "password": "SecurePass123!"
                    }
                    """))
        .andExpect(status().isForbidden());
  }
}
