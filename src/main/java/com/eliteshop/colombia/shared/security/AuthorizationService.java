package com.eliteshop.colombia.shared.security;

import com.eliteshop.colombia.shared.exception.ResourceAccessDeniedException;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/** Provides the common ownership checks used by resource adapters. */
@Component
public class AuthorizationService {

  /** Requires that the authenticated principal owns the requested customer resource. */
  public UUID requireCustomer(Authentication authentication, UUID customerId) {
    UUID authenticatedCustomerId = authenticatedUserId(authentication);
    if (!authenticatedCustomerId.equals(customerId)) {
      throw new ResourceAccessDeniedException("No tienes permisos sobre este cliente");
    }
    return authenticatedCustomerId;
  }

  /** Requires that the authenticated principal owns the requested seller resource. */
  public UUID requireSeller(Authentication authentication, UUID sellerId, String tokenSellerId) {
    if (!hasRole(authentication, "ROLE_SELLER")
        || tokenSellerId == null
        || !sellerId.toString().equals(tokenSellerId)) {
      throw new ResourceAccessDeniedException("No tienes permisos sobre este vendedor");
    }
    return sellerId;
  }

  /** Requires the requested role. */
  public void requireRole(Authentication authentication, String role) {
    if (!hasRole(authentication, role)) {
      throw new ResourceAccessDeniedException("No tienes permisos para realizar esta operación");
    }
  }

  /** Returns the authenticated user identifier. */
  public UUID authenticatedUserId(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new ResourceAccessDeniedException("La autenticación es obligatoria");
    }
    try {
      return UUID.fromString(authentication.getName());
    } catch (IllegalArgumentException exception) {
      throw new ResourceAccessDeniedException("La identidad autenticada no es válida");
    }
  }

  private boolean hasRole(Authentication authentication, String role) {
    return authentication != null
        && authentication.getAuthorities().stream()
            .anyMatch(authority -> role.equals(authority.getAuthority()));
  }
}
