package com.eliteshop.colombia.customer.infrastructure.controller.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCustomerRequest {

  @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
  private String firstName;

  @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
  private String lastName;

  @Size(min = 1, max = 20, message = "Phone number must be between 1 and 20 characters")
  private String phoneNumber;

  private String profileImage;

  @Size(min = 1, max = 20, message = "DNI type must be between 1 and 20 characters")
  private String dniType;

  @Size(min = 1, max = 20, message = "DNI number must be between 1 and 20 characters")
  private String dniNumber;

  @Size(min = 1, max = 150, message = "Address must be between 1 and 150 characters")
  private String address;

  @Size(min = 1, max = 50, message = "Department must be between 1 and 50 characters")
  private String department;

  @Size(min = 1, max = 60, message = "City must be between 1 and 60 characters")
  private String city;
}
