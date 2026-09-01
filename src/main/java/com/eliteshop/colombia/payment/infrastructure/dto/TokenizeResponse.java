package com.eliteshop.colombia.payment.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TokenizeResponse {

  private String id;
  private CardData card;

  @Data
  public static class CardData {

    @JsonProperty("exp_month")
    private String expMonth;

    @JsonProperty("exp_year")
    private String expYear;

    private String name;

    private String mask;
  }

  public String getLast4() {
    if (card == null || card.getMask() == null) return null;
    int idx = card.getMask().lastIndexOf("*");
    return card.getMask().substring(idx + 1);
  }
}
