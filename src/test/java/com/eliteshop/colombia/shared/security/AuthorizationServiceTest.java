package com.eliteshop.colombia.shared.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.eliteshop.colombia.shared.exception.ResourceAccessDeniedException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class AuthorizationServiceTest {

  private final AuthorizationService authorizationService = new AuthorizationService();

  @Test
  void shouldReturnCustomerId_whenUserOwnsResource() {
    UUID customerId = UUID.randomUUID();
    Authentication authentication = authentication(customerId, "ROLE_CUSTOMER");

    assertThat(authorizationService.requireCustomer(authentication, customerId))
        .isEqualTo(customerId);
  }

  @Test
  void shouldReject_whenResourceOwnedByAnotherUser() {
    UUID authenticatedCustomerId = UUID.randomUUID();
    Authentication authentication = authentication(authenticatedCustomerId, "ROLE_CUSTOMER");

    assertThatThrownBy(
            () -> authorizationService.requireCustomer(authentication, UUID.randomUUID()))
        .isInstanceOf(ResourceAccessDeniedException.class);
  }

  @Test
  void shouldReject_whenSellerIdMismatch() {
    UUID sellerId = UUID.randomUUID();
    Authentication authentication = authentication(UUID.randomUUID(), "ROLE_SELLER");

    assertThatThrownBy(
            () ->
                authorizationService.requireSeller(
                    authentication, sellerId, UUID.randomUUID().toString()))
        .isInstanceOf(ResourceAccessDeniedException.class);
  }

  @Test
  void shouldReject_whenAuthenticationIsMissing() {
    assertThatThrownBy(() -> authorizationService.authenticatedUserId(null))
        .isInstanceOf(ResourceAccessDeniedException.class);
  }

  private Authentication authentication(UUID userId, String role) {
    return new UsernamePasswordAuthenticationToken(
        userId.toString(), null, List.of(new SimpleGrantedAuthority(role)));
  }
}
