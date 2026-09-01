package com.eliteshop.colombia.payment.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CreateCustomerResponse {

  private boolean success;
  private CustomerData data;

  @Data
  public static class CustomerData {

    @JsonProperty("customerId")
    private String customerId;
  }
}
