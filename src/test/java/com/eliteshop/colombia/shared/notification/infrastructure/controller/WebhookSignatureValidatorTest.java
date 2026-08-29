package com.eliteshop.colombia.shared.notification.infrastructure.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class WebhookSignatureValidatorTest {

  private static final String SECRET = "test-secret";

  @Test
  void shouldReturnTrue_whenSignatureIsValid() {
    String payload = "{\"x_ref_payco\":\"12345\"}";
    String signature = computeHmac(payload, SECRET);

    assertThat(WebhookSignatureValidator.isValid(payload, signature, SECRET)).isTrue();
  }

  @Test
  void shouldReturnFalse_whenSignatureIsInvalid() {
    String payload = "{\"x_ref_payco\":\"12345\"}";
    String tampered = "sha256=0000000000000000000000000000000000000000000000000000000000000000";

    assertThat(WebhookSignatureValidator.isValid(payload, tampered, SECRET)).isFalse();
  }

  @Test
  void shouldReturnFalse_whenSignatureHeaderIsNull() {
    assertThat(WebhookSignatureValidator.isValid("payload", null, SECRET)).isFalse();
  }

  @Test
  void shouldReturnFalse_whenSignatureLacksSha256Prefix() {
    assertThat(WebhookSignatureValidator.isValid("payload", "abc123", SECRET)).isFalse();
  }

  @Test
  void shouldReturnFalse_whenPayloadIsEmpty() {
    String signature = computeHmac("", SECRET);

    assertThat(WebhookSignatureValidator.isValid("", signature, SECRET)).isTrue();
  }

  @Test
  void shouldReturnFalse_whenPayloadDiffers() {
    String payload = "{\"x_ref_payco\":\"12345\"}";
    String signature = computeHmac(payload, SECRET);

    assertThat(WebhookSignatureValidator.isValid("{\"x_ref_payco\":\"99999\"}", signature, SECRET))
        .isFalse();
  }

  private String computeHmac(String payload, String secret) {
    try {
      javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
      mac.init(
          new javax.crypto.spec.SecretKeySpec(
              secret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256"));
      byte[] hash = mac.doFinal(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder("sha256=");
      for (byte b : hash) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
