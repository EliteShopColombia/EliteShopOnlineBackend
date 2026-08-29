package com.eliteshop.colombia.seller.infrastructure.persistence;

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
@Table(name = "seller")
public class SellerEntity {

  @Id
  @Column(name = "seller_id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "seller_type_trade", nullable = false, length = 255)
  private String typeTrade;

  @Column(name = "seller_type_dni", nullable = false, length = 20)
  private String typeDni;

  @Column(name = "seller_dni_number", nullable = false, unique = true, length = 20)
  private String dniNumber;

  @Column(name = "seller_trade_name", nullable = false, length = 100)
  private String tradeName;

  @Column(name = "seller_fullname", nullable = false, length = 255)
  private String fullname;

  @Column(name = "seller_is_active", nullable = false)
  private Boolean isActive;

  @Column(name = "seller_is_verified", nullable = false)
  private Boolean isVerified;

  @Column(name = "seller_created_at", nullable = false, updatable = false)
  private Timestamp createdAt;

  @Column(name = "seller_update_at", nullable = true)
  private Timestamp updatedAt;

  @Column(name = "seller_profile_image", nullable = true, length = 500)
  private String profileImage;

  @OneToOne(mappedBy = "seller", cascade = CascadeType.ALL)
  private SellerContactEntity contact;

  @OneToOne(mappedBy = "seller", cascade = CascadeType.ALL)
  private SellerBankInfoEntity bankInfo;
}
