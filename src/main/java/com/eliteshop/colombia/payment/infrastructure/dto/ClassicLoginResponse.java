package com.eliteshop.colombia.payment.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ClassicLoginResponse {

  @JsonProperty("bearer_token")
  private String bearerToken;
}
