package com.eliteshop.colombia.customer.infrastructure.controller.dto;

import java.sql.Timestamp;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerResponse {

  private UUID id;
  private String firstName;
  private String lastName;
  private String email;
  private String phoneNumber;
  private String profileImage;
  private Timestamp createdAt;
  private Timestamp updatedAt;
  private String dniType;
  private String dniNumber;
  private String address;
  private String department;
  private String city;
  private Timestamp dniCreatedAt;
  private Timestamp dniUpdatedAt;
}
