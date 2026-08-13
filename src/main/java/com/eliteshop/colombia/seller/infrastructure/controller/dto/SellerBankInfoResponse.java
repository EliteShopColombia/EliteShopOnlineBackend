package com.eliteshop.colombia.seller.infrastructure.controller.dto;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SellerBankInfoResponse {

  private UUID sellerId;
  private String tradeName;
  private String fullname;
  private String bankName;
  private String typeBankAccount;
  private String numberAccount;
}
