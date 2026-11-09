package com.eliteshop.colombia.auth.infrastructure.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

  @NotBlank(message = "El email es requerido")
  @Email(message = "El email debe ser válido")
  private String email;

  @NotBlank(message = "La contraseña es requerida")
  private String password;
}
