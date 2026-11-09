package com.eliteshop.colombia.customer.infrastructure.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerRequest {

  @NotBlank(message = "El nombre es requerido")
  @Size(min = 1, max = 100, message = "El nombre debe tener entre 1 y 100 caracteres")
  private String firstName;

  @NotBlank(message = "El apellido es requerido")
  @Size(min = 1, max = 100, message = "El apellido debe tener entre 1 y 100 caracteres")
  private String lastName;

  @NotBlank(message = "El email es requerido")
  @Email(message = "El email debe ser válido")
  private String email;

  @NotBlank(message = "El teléfono es requerido")
  @Size(min = 1, max = 20, message = "El teléfono debe tener entre 1 y 20 caracteres")
  private String phoneNumber;

  @NotBlank(message = "La contraseña es requerida")
  @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
  private String password;

  private String profileImage;

  @Size(min = 1, max = 20, message = "El tipo de documento debe tener entre 1 y 20 caracteres")
  private String dniType;

  @Size(min = 1, max = 20, message = "El número de documento debe tener entre 1 y 20 caracteres")
  private String dniNumber;

  @Size(min = 1, max = 150, message = "La dirección debe tener entre 1 y 150 caracteres")
  private String address;

  @Size(min = 1, max = 60, message = "El departamento debe tener entre 1 y 60 caracteres")
  private String department;

  @Size(min = 1, max = 60, message = "La ciudad debe tener entre 1 y 60 caracteres")
  private String city;
}
