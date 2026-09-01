package com.eliteshop.colombia.admin.infrastructure.controller.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminRegisterResponse {

  private UUID id;
  private String email;
  private String firstName;
  private String lastName;
  private String role;
}
