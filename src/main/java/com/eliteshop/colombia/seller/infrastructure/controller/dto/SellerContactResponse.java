package com.eliteshop.colombia.seller.infrastructure.controller.dto;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SellerContactResponse {

  private UUID sellerId;
  private String tradeName;
  private String fullname;
  private String email;
  private String phoneNumber;
  private String tradeAddress;
  private String tradeDepartment;
  private String tradeCity;
}
