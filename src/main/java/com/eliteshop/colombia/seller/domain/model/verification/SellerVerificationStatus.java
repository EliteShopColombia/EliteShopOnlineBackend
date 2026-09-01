package com.eliteshop.colombia.seller.domain.model.verification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class SellerVerificationStatus {
  private final String value;

  public static SellerVerificationStatus pending() {
    return new SellerVerificationStatus("PENDING");
  }

  public static SellerVerificationStatus documentUploaded() {
    return new SellerVerificationStatus("DOCUMENT_UPLOADED");
  }

  public static SellerVerificationStatus selfieUploaded() {
    return new SellerVerificationStatus("SELFIE_UPLOADED");
  }

  public static SellerVerificationStatus approved() {
    return new SellerVerificationStatus("APPROVED");
  }

  public static SellerVerificationStatus rejected() {
    return new SellerVerificationStatus("REJECTED");
  }
}
