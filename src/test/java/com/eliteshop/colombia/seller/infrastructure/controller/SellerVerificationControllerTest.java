package com.eliteshop.colombia.seller.infrastructure.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.eliteshop.colombia.seller.application.usecase.VerifySellerUseCase;
import com.eliteshop.colombia.seller.domain.model.verification.*;
import com.eliteshop.colombia.seller.infrastructure.controller.verification.SellerVerificationController;
import com.eliteshop.colombia.seller.infrastructure.controller.verification.SellerVerificationMapperResponse;
import com.eliteshop.colombia.shared.exception.GlobalExceptionHandler;
import com.eliteshop.colombia.shared.exception.ResourceAccessDeniedException;
import com.eliteshop.colombia.shared.security.AuthorizationService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class SellerVerificationControllerTest {

  private MockMvc mockMvc;
  private VerifySellerUseCase verifySellerUseCase;
  private AuthorizationService authorizationService;
  private UUID sellerId;

  @BeforeEach
  void setUp() {
    verifySellerUseCase = mock(VerifySellerUseCase.class);
    authorizationService = mock(AuthorizationService.class);
    SellerVerificationMapperResponse mapper = new SellerVerificationMapperResponse();

    org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor executor =
        new org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor();
    executor.setCorePoolSize(1);
    executor.setMaxPoolSize(1);
    executor.setQueueCapacity(1);
    executor.setThreadNamePrefix("test-");
    executor.setRejectedExecutionHandler(
        new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
    executor.initialize();

    SellerVerificationController controller =
        new SellerVerificationController(
            verifySellerUseCase, mapper, executor, authorizationService);

    mockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    sellerId = UUID.randomUUID();
  }

  // ==================== POST /validate (async) ====================

  @Test
  void validate_shouldReturn202WithTaskId() throws Exception {
    String taskId = "task-" + UUID.randomUUID();
    SellerVerification verification = buildVerification(sellerId, "SELFIE_UPLOADED", taskId);

    when(verifySellerUseCase.submitVerification(sellerId)).thenReturn(verification);

    mockMvc
        .perform(
            post("/api/v1/sellers/" + sellerId + "/verification/validate")
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(new SimpleGrantedAuthority("ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.status").value("PROCESSING"))
        .andExpect(jsonPath("$.taskId").value(taskId))
        .andExpect(jsonPath("$.message").isNotEmpty());
  }

  @Test
  void validate_shouldReturn400WhenStatusNotSelfieUploaded() throws Exception {
    SellerVerification verification = buildVerification(sellerId, "PENDING", null);

    when(verifySellerUseCase.submitVerification(sellerId))
        .thenThrow(
            new com.eliteshop.colombia.seller.domain.exception.SellerVerificationException(
                "Primero sube la cedula y la selfie"));

    mockMvc
        .perform(
            post("/api/v1/sellers/" + sellerId + "/verification/validate")
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(new SimpleGrantedAuthority("ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isBadRequest());
  }

  // ==================== GET /verification ====================

  @Test
  void getStatus_shouldReturnVerification() throws Exception {
    SellerVerification verification = buildVerification(sellerId, "APPROVED", "task-123");
    verification = verification.approved(new SellerVerificationConfidenceScore(0.92));

    when(verifySellerUseCase.getStatus(sellerId)).thenReturn(verification);

    mockMvc
        .perform(
            get("/api/v1/sellers/" + sellerId + "/verification")
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(new SimpleGrantedAuthority("ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("APPROVED"))
        .andExpect(jsonPath("$.taskId").value("task-123"));
  }

  @Test
  void getStatus_shouldPollWhenProcessing() throws Exception {
    SellerVerification processing = buildVerification(sellerId, "PROCESSING", "task-poll");
    SellerVerification approved = buildVerification(sellerId, "APPROVED", "task-poll");
    approved =
        approved
            .approved(new SellerVerificationConfidenceScore(0.95))
            .withDetailedResult(
                new SellerVerificationConfidenceScore(0.95),
                new SellerVerificationLivenessConfidence(0.98),
                new SellerVerificationAntispoofScore(0.96),
                new SellerVerificationOcrCedulaNumber("12345678"),
                new SellerVerificationOcrCedulaName("Juan Perez"));

    when(verifySellerUseCase.getStatus(sellerId)).thenReturn(processing);
    when(verifySellerUseCase.pollVerificationResult(sellerId)).thenReturn(approved);

    mockMvc
        .perform(
            get("/api/v1/sellers/" + sellerId + "/verification")
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(new SimpleGrantedAuthority("ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("APPROVED"))
        .andExpect(jsonPath("$.confidenceScore").value(0.95));
  }

  // ==================== POST /document ====================

  @Test
  void uploadDocument_shouldReturn200() throws Exception {
    SellerVerification verification = buildVerification(sellerId, "DOCUMENT_UPLOADED", null);

    when(verifySellerUseCase.uploadDocument(eq(sellerId), anyString(), any()))
        .thenReturn(verification);

    MockMultipartFile file =
        new MockMultipartFile("file", "cedula.jpg", "image/jpeg", "test-content".getBytes());

    mockMvc
        .perform(
            multipart("/api/v1/sellers/" + sellerId + "/verification/document")
                .file(file)
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(new SimpleGrantedAuthority("ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("DOCUMENT_UPLOADED"));
  }

  @Test
  void uploadDocument_shouldReturn400WhenFileIsEmpty() throws Exception {
    MockMultipartFile file = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

    mockMvc
        .perform(
            multipart("/api/v1/sellers/" + sellerId + "/verification/document")
                .file(file)
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(new SimpleGrantedAuthority("ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isBadRequest());
  }

  // ==================== POST /validate-legacy ====================

  @Test
  void validateLegacy_shouldReturn202() throws Exception {
    SellerVerification verification = buildVerification(sellerId, "SELFIE_UPLOADED", null);
    when(verifySellerUseCase.getStatus(sellerId)).thenReturn(verification);

    mockMvc
        .perform(
            post("/api/v1/sellers/" + sellerId + "/verification/validate-legacy")
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(new SimpleGrantedAuthority("ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.status").value("PROCESSING"));
  }

  // ==================== Security ====================

  @Test
  void validate_shouldReturn403WhenCustomerRole() throws Exception {
    when(authorizationService.requireSeller(any(), eq(sellerId), any()))
        .thenThrow(new ResourceAccessDeniedException("No tienes permisos sobre este vendedor"));

    mockMvc
        .perform(
            post("/api/v1/sellers/" + sellerId + "/verification/validate")
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user("other-user")
                        .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isForbidden());
  }

  // ==================== Helpers ====================

  private SellerVerification buildVerification(UUID sellerId, String status, String taskId) {
    return new SellerVerification(
        SellerVerificationId.generate(),
        new SellerVerificationSellerId(sellerId),
        new SellerVerificationType("FACIAL_MATCH"),
        new SellerVerificationDocumentType("CC"),
        new SellerVerificationDocumentNumber("12345678"),
        new SellerVerificationDocumentMinioKey("sellers/" + sellerId + "/document.jpg"),
        new SellerVerificationSelfieMinioKey("sellers/" + sellerId + "/selfie.jpg"),
        new SellerVerificationStatus(status),
        null,
        null,
        new SellerVerificationCreatedAt(Instant.now()),
        null,
        taskId != null ? new SellerVerificationTaskId(taskId) : null,
        null,
        null,
        null,
        null);
  }
}
