package com.eliteshop.colombia.customer.infrastructure.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "customer")
public class CustomerEntity {

  @Id
  @Column(name = "customer_id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "customer_first_name", nullable = false)
  private String firstName;

  @Column(name = "customer_last_name", nullable = false)
  private String lastName;

  @Column(name = "customer_email", nullable = false, unique = true)
  private String email;

  @Column(name = "customer_phone_number", nullable = false)
  private String phoneNumber;

  @JsonIgnore
  @Column(name = "customer_password", nullable = false)
  private String password;

  @Column(name = "customer_profile_image")
  private String profileImage;

  @Column(name = "customer_role", nullable = false)
  private String role;

  @Column(name = "customer_created_at", nullable = false, updatable = false)
  private Timestamp createdAt;

  @Column(name = "customer_update_at")
  private Timestamp updatedAt;

  @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL)
  private CustomerInfoEntity info;
}
