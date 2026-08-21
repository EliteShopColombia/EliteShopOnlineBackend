package com.eliteshop.colombia.payment.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ChargeResponse {

  private boolean success;
  private ChargeData data;

  @Data
  public static class ChargeData {

    @JsonProperty("ref_payco")
    private String refId;

    private String estado;

    private String respuesta;
  }
}
