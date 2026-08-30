package com.eliteshop.colombia.seller.infrastructure.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "seller_bank_info")
public class SellerBankInfoEntity {

  @Id
  @Column(name = "seller_id", updatable = false, nullable = false)
  private UUID id;

  @JsonIgnore
  @OneToOne
  @MapsId
  @JoinColumn(name = "seller_id", nullable = false, unique = true)
  private SellerEntity seller;

  @Column(name = "bank_name", nullable = false, length = 150)
  private String bankName;

  @Column(name = "type_account", nullable = false, length = 20)
  private String typeAccount;

  @Column(name = "number_account", nullable = false, length = 30)
  private String numberAccount;
}
