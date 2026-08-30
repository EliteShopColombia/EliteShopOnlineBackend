package com.eliteshop.colombia.customer.infrastructure.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerRequest {

  @NotBlank(message = "First name is required")
  @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
  private String firstName;

  @NotBlank(message = "Last name is required")
  @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
  private String lastName;

  @NotBlank(message = "Email is required")
  @Email(message = "Email must be valid")
  private String email;

  @NotBlank(message = "Phone number is required")
  @Size(min = 1, max = 20, message = "Phone number must be between 1 and 20 characters")
  private String phoneNumber;

  @NotBlank(message = "Password is required")
  @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
  private String password;

  private String profileImage;

  @Size(min = 1, max = 20, message = "DNI type must be between 1 and 20 characters")
  private String dniType;

  @Size(min = 1, max = 20, message = "DNI number must be between 1 and 20 characters")
  private String dniNumber;

  @Size(min = 1, max = 150, message = "Address must be between 1 and 150 characters")
  private String address;

  @Size(min = 1, max = 60, message = "Department must be between 1 and 60 characters")
  private String department;

  @Size(min = 1, max = 60, message = "City must be between 1 and 60 characters")
  private String city;
}
