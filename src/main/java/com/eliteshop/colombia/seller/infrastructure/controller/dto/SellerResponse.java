package com.eliteshop.colombia.seller.infrastructure.controller.dto;

import java.sql.Timestamp;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SellerResponse {

  private UUID id;
  private String typeTrade;
  private String typeDni;
  private String dniNumber;
  private String tradeName;
  private String fullname;
  private Boolean isActive;
  private Boolean isVerified;
  private Timestamp createdAt;
  private Timestamp updatedAt;
  private String profileImage;
}
