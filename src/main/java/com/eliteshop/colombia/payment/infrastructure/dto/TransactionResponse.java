package com.eliteshop.colombia.payment.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TransactionResponse {

  @JsonProperty("ref_epayco")
  private String refId;

  @JsonProperty("x_respuesta")
  private String status;

  @JsonProperty("x_amount")
  private Long amount;

  @JsonProperty("x_currency_code")
  private String currency;

  @JsonProperty("x_approval_code")
  private String approvalCode;

  @JsonProperty("x_transaction_id")
  private String transactionId;
}
