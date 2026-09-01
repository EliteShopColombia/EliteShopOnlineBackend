package com.eliteshop.colombia.auth.infrastructure.controller.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponse {

  private String token;
  private long expiresAt;
  private UserInfo user;

  @Getter
  @AllArgsConstructor
  public static class UserInfo {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
  }
}
