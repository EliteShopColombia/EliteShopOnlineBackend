package com.eliteshop.colombia.seller.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "seller_contact")
public class SellerContactEntity {

  @Id
  @Column(name = "seller_id", updatable = false, nullable = false)
  private UUID id;

  @OneToOne
  @MapsId
  @JoinColumn(name = "seller_id", nullable = false, unique = true)
  private SellerEntity seller;

  @Column(name = "seller_email", nullable = false, length = 150)
  private String email;

  @Column(name = "seller_phone_number", nullable = false, length = 15)
  private String phoneNumber;

  @Column(name = "seller_trade_address", nullable = false, length = 150)
  private String tradeAddress;

  @Column(name = "seller_trade_department", nullable = false, length = 50)
  private String tradeDepartment;

  @Column(name = "seller_trade_city", nullable = false, length = 60)
  private String tradeCity;
}
