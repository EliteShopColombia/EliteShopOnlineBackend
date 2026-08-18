package com.eliteshop.colombia.auth.infrastructure.controller;

import com.eliteshop.colombia.auth.application.LoginUseCase;
import com.eliteshop.colombia.auth.application.RefreshUseCase;
import com.eliteshop.colombia.auth.application.RegisterUseCase;
import com.eliteshop.colombia.auth.infrastructure.config.JwtService;
import com.eliteshop.colombia.auth.infrastructure.controller.dto.AuthResponse;
import com.eliteshop.colombia.auth.infrastructure.controller.dto.LoginRequest;
import com.eliteshop.colombia.auth.infrastructure.controller.dto.RegisterRequest;
import com.eliteshop.colombia.auth.infrastructure.mapper.AuthMapper;
import com.eliteshop.colombia.customer.domain.model.Customer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final RegisterUseCase registerUseCase;
  private final LoginUseCase loginUseCase;
  private final RefreshUseCase refreshUseCase;
  private final JwtService jwtService;
  private final AuthMapper authMapper;

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
    Customer customer = authMapper.toDomainFromRegisterRequest(request);
    registerUseCase.execute(customer);
    String token =
        jwtService.generateToken(
            customer.getId().getValue().toString(), customer.getEmail().getValue(), "customer");
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(authMapper.toAuthResponse(customer, token, jwtService.getExpiration()));
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    Customer customer = loginUseCase.execute(request.getEmail(), request.getPassword());
    String token =
        jwtService.generateToken(
            customer.getId().getValue().toString(), customer.getEmail().getValue(), "customer");
    return ResponseEntity.ok(
        authMapper.toAuthResponse(customer, token, jwtService.getExpiration()));
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> refresh(
      @RequestHeader("Authorization") String authorization) {
    String token = authorization.replace("Bearer ", "");
    Customer customer = refreshUseCase.execute(token);
    String newToken =
        jwtService.generateToken(
            customer.getId().getValue().toString(), customer.getEmail().getValue(), "customer");
    return ResponseEntity.ok(
        authMapper.toAuthResponse(customer, newToken, jwtService.getExpiration()));
  }
}
