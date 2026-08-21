package com.eliteshop.colombia.auth.application;

import com.eliteshop.colombia.auth.domain.exception.InvalidCredentialsException;
import com.eliteshop.colombia.auth.infrastructure.config.JwtService;
import com.eliteshop.colombia.customer.domain.model.Customer;
import com.eliteshop.colombia.customer.domain.model.CustomerId;
import com.eliteshop.colombia.customer.domain.repository.CustomerRepository;
import io.jsonwebtoken.Claims;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class RefreshUseCase {

  private final CustomerRepository customerRepository;
  private final JwtService jwtService;

  public Customer execute(String token) {
    log.info("Iniciando refresh de token");
    try {
      Claims claims = jwtService.validateToken(token);
      UUID userId = UUID.fromString(claims.getSubject());

      Customer customer =
          customerRepository
              .findById(new CustomerId(userId))
              .orElseThrow(() -> new InvalidCredentialsException("User not found"));

      log.info("Refresh exitoso para userId: {}", userId);
      return customer;
    } catch (InvalidCredentialsException e) {
      log.error("Error en refresh de token: {}", e.getMessage());
      throw e;
    }
  }
}
