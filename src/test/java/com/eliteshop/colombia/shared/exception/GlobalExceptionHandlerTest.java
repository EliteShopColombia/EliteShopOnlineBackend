package com.eliteshop.colombia.shared.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class GlobalExceptionHandlerTest {

  @Test
  void shouldContainTimestamp_whenCreatingErrorResponse() {
    ErrorResponse response =
        ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(404)
            .error("Not found")
            .code("NOT_FOUND")
            .build();

    assertThat(response.getTimestamp()).isNotNull();
    assertThat(response.getStatus()).isEqualTo(404);
    assertThat(response.getError()).isEqualTo("Not found");
    assertThat(response.getCode()).isEqualTo("NOT_FOUND");
  }

  @Test
  void shouldHaveNullFieldErrors_byDefault() {
    ErrorResponse response = ErrorResponse.of(400, "Bad request", "BAD_REQUEST");
    assertThat(response.getFieldErrors()).isNull();
  }

  @Test
  void shouldSetAllFields_whenUsingFactory() {
    ErrorResponse response = ErrorResponse.of(409, "Conflict", "CONFLICT");
    assertThat(response.getTimestamp()).isNotNull();
    assertThat(response.getStatus()).isEqualTo(409);
    assertThat(response.getError()).isEqualTo("Conflict");
    assertThat(response.getCode()).isEqualTo("CONFLICT");
  }

  @Test
  void shouldWork_whenBuilderHasNoTimestamp() {
    ErrorResponse response =
        ErrorResponse.builder().status(500).error("Internal error").code("INTERNAL_ERROR").build();

    assertThat(response.getTimestamp()).isNull();
    assertThat(response.getStatus()).isEqualTo(500);
    assertThat(response.getFieldErrors()).isNull();
  }
}
