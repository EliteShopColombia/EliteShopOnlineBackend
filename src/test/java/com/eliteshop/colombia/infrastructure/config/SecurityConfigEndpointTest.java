package com.eliteshop.colombia.infrastructure.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class SecurityConfigEndpointTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void shouldAllowAccessWhenEndpointIsPublic() throws Exception {
    mockMvc.perform(get("/api/v1/products")).andExpect(status().isOk());
  }

  @Test
  void shouldAllowAccessWhenAccessingActuatorHealth() throws Exception {
    mockMvc
        .perform(get("/actuator/health"))
        .andExpect(
            result ->
                org.assertj.core.api.Assertions.assertThat(result.getResponse().getStatus())
                    .isNotIn(401, 403));
  }

  @Test
  void shouldAllowAccessWhenSellerEndpoint() throws Exception {
    mockMvc.perform(get("/api/v1/sellers")).andExpect(status().isOk());
  }

  @Test
  void shouldAllowAccessWhenReviewEndpoint() throws Exception {
    mockMvc.perform(get("/api/v1/reviews")).andExpect(status().isOk());
  }

  @Test
  void shouldReturn403WhenCreatingProductWithoutAuth() throws Exception {
    mockMvc
        .perform(post("/api/v1/products").contentType("application/json").content("{}"))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "CUSTOMER")
  void shouldDenyWhenCustomerCreatesProduct() throws Exception {
    mockMvc
        .perform(post("/api/v1/products").contentType("application/json").content("{}"))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "SELLER")
  void shouldAllowWhenSellerAccessesSellerEndpoints() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/orders/00000000-0000-0000-0000-000000000000/tracking")
                .contentType("application/json")
                .content(
                    "{\"status\":\"PREPARING\",\"location\":\"Bogota\",\"description\":\"test\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(roles = "CUSTOMER")
  void shouldDenyWhenCustomerAccessesSellerEndpoints() throws Exception {
    mockMvc
        .perform(post("/api/v1/products").contentType("application/json").content("{}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void shouldReturn403WhenCreatingOrderWithoutAuth() throws Exception {
    mockMvc
        .perform(post("/api/v1/orders").contentType("application/json").content("{}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void shouldAllowAccessWhenWebhookEndpoint() throws Exception {
    mockMvc
        .perform(post("/webhooks/epayco").contentType("application/json").content("{}"))
        .andExpect(status().isUnauthorized());
  }
}
