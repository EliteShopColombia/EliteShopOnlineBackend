package com.eliteshop.colombia.customer.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "customer_info")
public class CustomerInfoEntity {

  @Id
  @Column(name = "customer_id", updatable = false, nullable = false)
  private UUID id;

  @OneToOne
  @MapsId
  @JoinColumn(name = "customer_id", nullable = false, unique = true)
  private CustomerEntity customer;

  @Column(name = "customer_dni_type", nullable = false)
  private String dniType;

  @Column(name = "customer_dni_number", nullable = false, unique = true)
  private String dniNumber;

  @Column(name = "customer_address", nullable = false)
  private String address;

  @Column(name = "customer_department", nullable = false)
  private String department;

  @Column(name = "customer_city", nullable = false)
  private String city;

  @Column(name = "customer_dni_created_at", nullable = false)
  private Timestamp dniCreatedAt;

  @Column(name = "customer_dni_update_at")
  private Timestamp dniUpdatedAt;
}
