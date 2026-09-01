package com.eliteshop.colombia.auth.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

  private JwtService jwtService;

  @BeforeEach
  void setUp() {
    JwtProperties props = new JwtProperties();
    props.setSecret("dGVzdC1zZWNyZXQtZm9yLXVuaXQtdGVzdHMtb25seS0z/Y2hhcnM=");
    props.setExpiration(86400000);
    props.setIssuer("eliteshop-test");
    jwtService = new JwtService(props);
  }

  @Test
  void shouldGenerateToken_withCorrectClaims() {
    String userId = "550e8400-e29b-41d4-a716-446655440000";
    String email = "test@example.com";
    String role = "customer";

    String token = jwtService.generateToken(userId, email, role);
    Claims claims = jwtService.validateToken(token);

    assertThat(claims.getSubject()).isEqualTo(userId);
    assertThat(claims.get("email", String.class)).isEqualTo(email);
    assertThat(claims.get("role", String.class)).isEqualTo(role);
    assertThat(claims.getIssuer()).isEqualTo("eliteshop-test");
  }

  @Test
  void shouldGenerateToken_withSellerId() {
    String userId = "550e8400-e29b-41d4-a716-446655440000";
    String email = "seller@example.com";
    String role = "seller";
    String sellerId = "660e8400-e29b-41d4-a716-446655440000";

    String token = jwtService.generateToken(userId, email, role, sellerId);
    Claims claims = jwtService.validateToken(token);

    assertThat(claims.get("sellerId", String.class)).isEqualTo(sellerId);
  }

  @Test
  void shouldGenerateToken_withoutSellerId() {
    String token =
        jwtService.generateToken("550e8400-e29b-41d4-a716-446655440000", "a@b.com", "customer");
    Claims claims = jwtService.validateToken(token);

    assertThat(claims.get("sellerId", String.class)).isNull();
  }

  @Test
  void shouldValidate_whenTokenIsValid() {
    String token =
        jwtService.generateToken("550e8400-e29b-41d4-a716-446655440000", "a@b.com", "customer");

    Claims claims = jwtService.validateToken(token);

    assertThat(claims).isNotNull();
    assertThat(claims.getSubject()).isNotBlank();
  }

  @Test
  void shouldReject_whenIssuerIsWrong() {
    JwtProperties props = new JwtProperties();
    props.setSecret("dGVzdC1zZWNyZXQtZm9yLXVuaXQtdGVzdHMtb25seS0z/Y2hhcnM=");
    props.setExpiration(86400000);
    props.setIssuer("different-issuer");
    JwtService otherService = new JwtService(props);

    String token =
        jwtService.generateToken("550e8400-e29b-41d4-a716-446655440000", "a@b.com", "customer");

    assertThatThrownBy(() -> otherService.validateToken(token)).isInstanceOf(JwtException.class);
  }

  @Test
  void shouldReject_whenSecretIsWrong() {
    JwtProperties props = new JwtProperties();
    props.setSecret("YW5vdGhlci1zZWNyZXQtZm9yLXVuaXQtdGVzdHMtb25seS0z/Y2hhcnM=");
    props.setExpiration(86400000);
    props.setIssuer("eliteshop-test");
    JwtService otherService = new JwtService(props);

    String token =
        jwtService.generateToken("550e8400-e29b-41d4-a716-446655440000", "a@b.com", "customer");

    assertThatThrownBy(() -> otherService.validateToken(token)).isInstanceOf(JwtException.class);
  }

  @Test
  void shouldReject_whenTokenIsTampered() {
    String token =
        jwtService.generateToken("550e8400-e29b-41d4-a716-446655440000", "a@b.com", "customer");
    String tampered = token.substring(0, token.length() - 5) + "XXXXX";

    assertThatThrownBy(() -> jwtService.validateToken(tampered)).isInstanceOf(JwtException.class);
  }

  @Test
  void shouldReject_whenTokenIsEmpty() {
    assertThatThrownBy(() -> jwtService.validateToken("")).isInstanceOf(Exception.class);
  }
}
