package com.eliteshop.colombia.payment.infrastructure.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eliteshop.colombia.payment.application.usecase.ConfirmPaymentUseCase;
import com.eliteshop.colombia.payment.domain.model.Payment;
import com.eliteshop.colombia.payment.domain.model.PaymentStatus;
import com.eliteshop.colombia.payment.infrastructure.config.GatewayProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class WebhookControllerTest {

  private MockMvc mockMvc;
  private ConfirmPaymentUseCase confirmPaymentUseCase;
  private GatewayProperties gatewayProperties;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    confirmPaymentUseCase = mock(ConfirmPaymentUseCase.class);
    gatewayProperties = mock(GatewayProperties.class);
    when(gatewayProperties.getSecret()).thenReturn("test-webhook-secret");

    WebhookController controller = new WebhookController(confirmPaymentUseCase, gatewayProperties);
    mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  @Test
  void shouldReturn200WhenWebhookIsValid() throws Exception {
    String body = "{\"x_ref_payco\":\"ref-123\",\"x_respuesta\":\"ACEPTADA\"}";
    String signature = computeHmac(body, "test-webhook-secret");

    Payment payment =
        Payment.builder()
            .id(UUID.randomUUID())
            .status(PaymentStatus.APPROVED)
            .epaycoRefId("ref-123")
            .build();
    when(confirmPaymentUseCase.execute("ref-123"))
        .thenReturn(reactor.core.publisher.Mono.just(payment));

    mockMvc
        .perform(
            post("/webhooks/epayco")
                .contentType("application/json")
                .content(body)
                .header("X-Signature", signature))
        .andExpect(status().isOk());

    verify(confirmPaymentUseCase).execute("ref-123");
  }

  @Test
  void shouldReturn401WhenSignatureIsInvalid() throws Exception {
    String body = "{\"x_ref_payco\":\"ref-123\"}";

    mockMvc
        .perform(
            post("/webhooks/epayco")
                .contentType("application/json")
                .content(body)
                .header("X-Signature", "sha256=invalidsig"))
        .andExpect(status().isUnauthorized());

    verify(confirmPaymentUseCase, never()).execute(any());
  }

  @Test
  void shouldReturn401WhenSignatureIsMissing() throws Exception {
    String body = "{\"x_ref_payco\":\"ref-123\"}";

    mockMvc
        .perform(post("/webhooks/epayco").contentType("application/json").content(body))
        .andExpect(status().isUnauthorized());

    verify(confirmPaymentUseCase, never()).execute(any());
  }

  @Test
  void shouldReturn503WhenGatewaySecretNotConfigured() throws Exception {
    when(gatewayProperties.getSecret()).thenReturn(null);

    mockMvc
        .perform(post("/webhooks/epayco").contentType("application/json").content("{}"))
        .andExpect(status().isServiceUnavailable());
  }

  @Test
  void shouldReturn503WhenGatewaySecretIsBlank() throws Exception {
    when(gatewayProperties.getSecret()).thenReturn("   ");

    mockMvc
        .perform(post("/webhooks/epayco").contentType("application/json").content("{}"))
        .andExpect(status().isServiceUnavailable());
  }

  @Test
  void shouldReturn200WhenRefIdIsNull() throws Exception {
    String body = "{\"x_respuesta\":\"ACEPTADA\"}";
    String signature = computeHmac(body, "test-webhook-secret");

    mockMvc
        .perform(
            post("/webhooks/epayco")
                .contentType("application/json")
                .content(body)
                .header("X-Signature", signature))
        .andExpect(status().isOk());

    verify(confirmPaymentUseCase, never()).execute(any());
  }

  @Test
  void shouldReturn200WhenHealthCheck() throws Exception {
    mockMvc
        .perform(get("/webhooks/epayco"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("ok"))
        .andExpect(jsonPath("$.service").value("epayco-webhook"));
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
