package com.eliteshop.colombia.payment.infrastructure.dto;

import lombok.Data;

@Data
public class CreateSessionResponse {
  private boolean success;
  private SessionData data;

  @Data
  public static class SessionData {
    private String sessionId;
    private String token;
  }
}
