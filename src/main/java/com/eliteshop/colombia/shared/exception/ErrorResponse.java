package com.eliteshop.colombia.shared.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

  private Instant timestamp;
  private int status;
  private String error;
  private String code;
  private Map<String, String> fieldErrors;

  public static ErrorResponse of(int status, String error, String code) {
    return ErrorResponse.builder()
        .timestamp(Instant.now())
        .status(status)
        .error(error)
        .code(code)
        .build();
  }
}
