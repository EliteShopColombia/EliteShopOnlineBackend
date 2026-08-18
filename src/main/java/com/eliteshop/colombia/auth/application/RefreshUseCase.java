package com.eliteshop.colombia.auth.application;

import com.eliteshop.colombia.auth.domain.exception.InvalidCredentialsException;
import com.eliteshop.colombia.auth.infrastructure.config.JwtService;
import com.eliteshop.colombia.customer.domain.model.Customer;
import com.eliteshop.colombia.customer.domain.model.CustomerId;
import com.eliteshop.colombia.customer.domain.repository.CustomerRepository;
import io.jsonwebtoken.Claims;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RefreshUseCase {

  private final CustomerRepository customerRepository;
  private final JwtService jwtService;

  public Customer execute(String token) {
    Claims claims = jwtService.validateToken(token);
    UUID userId = UUID.fromString(claims.getSubject());

    return customerRepository
        .findById(new CustomerId(userId))
        .orElseThrow(() -> new InvalidCredentialsException("User not found"));
  }
}
