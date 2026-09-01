package com.eliteshop.colombia.auth.infrastructure.filter;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eliteshop.colombia.auth.infrastructure.config.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class JwtAuthFilterTest {

  private MockMvc mockMvc;
  private JwtService jwtService;

  @BeforeEach
  void setUp() {
    jwtService = org.mockito.Mockito.mock(JwtService.class);
    mockMvc =
        MockMvcBuilders.standaloneSetup(new TestController())
            .addFilters(new JwtAuthFilter(jwtService))
            .build();
  }

  @Test
  void shouldPassThrough_whenNoAuthHeader() throws Exception {
    mockMvc.perform(get("/test")).andExpect(status().isOk());
  }

  @Test
  void shouldSetAuthentication_whenTokenIsValid() throws Exception {
    Claims claims =
        new DefaultClaims(
            Map.of(
                "sub",
                "550e8400-e29b-41d4-a716-446655440000",
                "email",
                "a@b.com",
                "role",
                "customer"));
    when(jwtService.validateToken("valid-token")).thenReturn(claims);

    mockMvc
        .perform(get("/test").header("Authorization", "Bearer valid-token"))
        .andExpect(status().isOk())
        .andExpect(request().attribute("gateway.userId", "550e8400-e29b-41d4-a716-446655440000"))
        .andExpect(request().attribute("gateway.userEmail", "a@b.com"))
        .andExpect(request().attribute("gateway.userRole", "customer"));
  }

  @Test
  void shouldReturn401_whenTokenIsInvalid() throws Exception {
    when(jwtService.validateToken("bad-token"))
        .thenThrow(new io.jsonwebtoken.JwtException("Invalid token"));

    mockMvc
        .perform(get("/test").header("Authorization", "Bearer bad-token"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void shouldReturn401_whenHeaderIsMalformed() throws Exception {
    when(jwtService.validateToken("malformed"))
        .thenThrow(new io.jsonwebtoken.JwtException("Malformed"));

    mockMvc
        .perform(get("/test").header("Authorization", "Bearer malformed"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void shouldReturn401_whenBearerIsEmpty() throws Exception {
    when(jwtService.validateToken(""))
        .thenThrow(new io.jsonwebtoken.JwtException("Malformed JWT string"));

    mockMvc
        .perform(get("/test").header("Authorization", "Bearer "))
        .andExpect(status().isUnauthorized());
  }

  @org.springframework.web.bind.annotation.RestController
  static class TestController {
    @org.springframework.web.bind.annotation.GetMapping("/test")
    public Map<String, String> test() {
      return Map.of("status", "ok");
    }
  }
}
