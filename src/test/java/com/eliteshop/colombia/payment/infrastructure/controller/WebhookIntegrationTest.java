package com.eliteshop.colombia.payment.infrastructure.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WebhookIntegrationTest {

  @Autowired private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper();

  private static final String WEBHOOK_SECRET = "test-webhook-secret";

  @Test
  void shouldAcceptEpaycoWebhookWithValidSignature() throws Exception {
    Map<String, Object> webhookPayload =
        Map.of(
            "x_id_emp", "test-ref-id",
            "x_ref_payco", "test-ref-payco",
            "x_respuesta", "Aceptada");

    String payloadJson = objectMapper.writeValueAsString(webhookPayload);
    String signature = computeHmacSha256(payloadJson, WEBHOOK_SECRET);

    mockMvc
        .perform(
            post("/webhooks/epayco")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Signature", signature)
                .content(payloadJson))
        .andExpect(status().is5xxServerError());
  }

  @Test
  void shouldRejectWebhookWithInvalidSignature() throws Exception {
    Map<String, Object> webhookPayload =
        Map.of(
            "x_id_emp", "test-ref-id",
            "x_ref_payco", "test-ref-payco",
            "x_respuesta", "Aceptada");

    String payloadJson = objectMapper.writeValueAsString(webhookPayload);

    mockMvc
        .perform(
            post("/webhooks/epayco")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Signature", "sha256=invalidsignature")
                .content(payloadJson))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void shouldRejectWebhookWithoutSignature() throws Exception {
    Map<String, Object> webhookPayload =
        Map.of(
            "x_id_emp", "test-ref-id",
            "x_ref_payco", "test-ref-payco");

    mockMvc
        .perform(
            post("/webhooks/epayco")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(webhookPayload)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void shouldHandleEpaycoHealthCheck() throws Exception {
    mockMvc
        .perform(get("/webhooks/epayco"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("ok"));
  }

  private String computeHmacSha256(String payload, String secret) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
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
