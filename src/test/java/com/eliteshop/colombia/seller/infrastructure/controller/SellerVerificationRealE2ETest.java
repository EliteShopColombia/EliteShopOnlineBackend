package com.eliteshop.colombia.seller.infrastructure.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.eliteshop.colombia.seller.infrastructure.persistence.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * End-to-end tests REALES contra el microservicio de verificación facial (face-matcher) desplegado
 * en Cloudflare tunnel y MinIO real. No usa mocks.
 *
 * <p>Requisitos:
 *
 * <ul>
 *   <li>MinIO corriendo en localhost:32768 (user: minioadmin/pass: minioadmin)
 *   <li>Face-matcher desplegado y accesible via Cloudflare tunnel
 *   <li>Bucket eliteshop-sellers-test existente en MinIO
 * </ul>
 *
 * <p>WARNING: Estos tests son lentos (~15-60s por test) porque dependen de servicios externos
 * reales.
 */
@Tag("e2e")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("e2e")
@Timeout(300)
class SellerVerificationRealE2ETest {

  private static final String BASE_PATH = "/api/v1/sellers/%s/verification";
  private static final String CEDULA_JPG = "/home/arch-tarok/Documentos/Prueba/Cedula.jpeg";
  private static final String FOTO_JPG = "/home/arch-tarok/Documentos/Prueba/Foto.jpeg";
  private static final String CEDULA2_JPG = "/home/arch-tarok/Documentos/Prueba/Cedula2.jpeg";
  private static final String FOTO2_JPG = "/home/arch-tarok/Documentos/Prueba/Foto2.jpeg";
  private static final String CEDULA3_JPG = "/home/arch-tarok/Documentos/Prueba/Cedula3.jpeg";
  private static final String FOTO10_JPG = "/home/arch-tarok/Documentos/Prueba/Foto10.jpeg";

  @Autowired private MockMvc mockMvc;
  @Autowired private SellerJpaRepository sellerJpaRepository;
  @Autowired private SellerVerificationJpaRepository verificationJpaRepository;

  private UUID sellerId;
  private static final AtomicInteger DNI_COUNTER = new AtomicInteger(90000000);
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    sellerId = UUID.randomUUID();
    SellerEntity seller = createSellerEntity(sellerId);
    SellerContactEntity contact = createContactEntity(sellerId, seller);
    seller.setContact(contact);
    sellerJpaRepository.save(seller);
  }

  @AfterEach
  void cleanUp() {
    verificationJpaRepository.findBySellerId(sellerId).ifPresent(verificationJpaRepository::delete);
    sellerJpaRepository.deleteById(sellerId);
  }

  // ==================== Happy Path: Flujo completo real ====================

  @Test
  void e2e_real_fullFlow_uploadDocUploadSelfieValidatePollApproved() throws Exception {
    // Paso 1: Subir cedula real a MinIO
    MockMultipartFile cedulaFile = createMultipartFile(CEDULA_JPG, "cedula.jpeg");
    mockMvc
        .perform(
            multipart(String.format(BASE_PATH, sellerId) + "/document")
                .file(cedulaFile)
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("DOCUMENT_UPLOADED"));

    // Paso 2: Subir selfie real a MinIO
    MockMultipartFile selfieFile = createMultipartFile(FOTO_JPG, "selfie.jpeg");
    mockMvc
        .perform(
            multipart(String.format(BASE_PATH, sellerId) + "/selfie")
                .file(selfieFile)
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SELFIE_UPLOADED"));

    // Paso 3: Validar (debe retornar taskId)
    MvcResult validateResult =
        mockMvc
            .perform(
                post(String.format(BASE_PATH, sellerId) + "/validate")
                    .with(
                        org.springframework.security.test.web.servlet.request
                            .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                            .authorities(
                                new org.springframework.security.core.authority
                                    .SimpleGrantedAuthority("ROLE_SELLER")))
                    .requestAttr("gateway.sellerId", sellerId.toString()))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.status").value("PROCESSING"))
            .andExpect(jsonPath("$.taskId").isNotEmpty())
            .andReturn();

    String taskId =
        objectMapper
            .readTree(validateResult.getResponse().getContentAsByteArray())
            .get("taskId")
            .asText();
    System.out.println("=== E2E REAL: taskId=" + taskId + " ===");

    // Paso 4: Polling con reintentos (el microservicio necesita tiempo para procesar)
    boolean foundFinalStatus = false;
    for (int i = 1; i <= 20; i++) {
      Thread.sleep(5000); // Esperar 5 segundos entre polls

      MvcResult pollResult =
          mockMvc
              .perform(
                  get(String.format(BASE_PATH, sellerId))
                      .with(
                          org.springframework.security.test.web.servlet.request
                              .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                              .authorities(
                                  new org.springframework.security.core.authority
                                      .SimpleGrantedAuthority("ROLE_SELLER")))
                      .requestAttr("gateway.sellerId", sellerId.toString()))
              .andExpect(status().isOk())
              .andReturn();

      String responseBody = pollResult.getResponse().getContentAsString();
      JsonNode json = objectMapper.readTree(responseBody);
      String status = json.get("status").asText();

      System.out.println("=== E2E REAL: Poll " + i + "/20 - status=" + status + " ===");

      if ("APPROVED".equals(status) || "REJECTED".equals(status)) {
        System.out.println("=== E2E REAL: Resultado final: " + status + " ===");
        System.out.println("=== E2E REAL: Response body: " + responseBody + " ===");
        foundFinalStatus = true;

        // Verificar campos de respuesta
        if ("APPROVED".equals(status)) {
          org.assertj.core.api.Assertions.assertThat(json.has("confidenceScore")).isTrue();
          org.assertj.core.api.Assertions.assertThat(json.get("confidenceScore").asDouble())
              .isGreaterThan(0.0);
          System.out.println(
              "=== E2E REAL: confidenceScore=" + json.get("confidenceScore").asDouble() + " ===");
          if (json.has("livenessConfidence") && !json.get("livenessConfidence").isNull()) {
            System.out.println(
                "=== E2E REAL: livenessConfidence="
                    + json.get("livenessConfidence").asDouble()
                    + " ===");
          }
          if (json.has("antispoofScore") && !json.get("antispoofScore").isNull()) {
            System.out.println(
                "=== E2E REAL: antispoofScore=" + json.get("antispoofScore").asDouble() + " ===");
          }
          if (json.has("ocrCedulaNumber") && !json.get("ocrCedulaNumber").isNull()) {
            System.out.println(
                "=== E2E REAL: ocrCedulaNumber=" + json.get("ocrCedulaNumber").asText() + " ===");
          }
          if (json.has("ocrCedulaName") && !json.get("ocrCedulaName").isNull()) {
            System.out.println(
                "=== E2E REAL: ocrCedulaName=" + json.get("ocrCedulaName").asText() + " ===");
          }
        }
        break;
      }
    }

    org.assertj.core.api.Assertions.assertThat(foundFinalStatus)
        .as("El microservicio debió devolver un estado final (APPROVED o REJECTED)")
        .isTrue();
  }

  // ==================== Flujo con cedula2 y foto2 ====================

  @Test
  void e2e_real_validate_withCedula2AndFoto2() throws Exception {
    // Subir cedula2
    MockMultipartFile cedulaFile = createMultipartFile(CEDULA2_JPG, "cedula2.jpeg");
    mockMvc
        .perform(
            multipart(String.format(BASE_PATH, sellerId) + "/document")
                .file(cedulaFile)
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("DOCUMENT_UPLOADED"));

    // Subir selfie2
    MockMultipartFile selfieFile = createMultipartFile(FOTO2_JPG, "selfie2.jpeg");
    mockMvc
        .perform(
            multipart(String.format(BASE_PATH, sellerId) + "/selfie")
                .file(selfieFile)
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SELFIE_UPLOADED"));

    // Validar
    MvcResult validateResult =
        mockMvc
            .perform(
                post(String.format(BASE_PATH, sellerId) + "/validate")
                    .with(
                        org.springframework.security.test.web.servlet.request
                            .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                            .authorities(
                                new org.springframework.security.core.authority
                                    .SimpleGrantedAuthority("ROLE_SELLER")))
                    .requestAttr("gateway.sellerId", sellerId.toString()))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.status").value("PROCESSING"))
            .andReturn();

    String taskId =
        objectMapper
            .readTree(validateResult.getResponse().getContentAsByteArray())
            .get("taskId")
            .asText();
    System.out.println("=== E2E REAL (c2f2): taskId=" + taskId + " ===");

    // Polling
    boolean foundFinalStatus = false;
    for (int i = 1; i <= 20; i++) {
      Thread.sleep(5000); // Esperar 5 segundos entre polls
      MvcResult pollResult =
          mockMvc
              .perform(
                  get(String.format(BASE_PATH, sellerId))
                      .with(
                          org.springframework.security.test.web.servlet.request
                              .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                              .authorities(
                                  new org.springframework.security.core.authority
                                      .SimpleGrantedAuthority("ROLE_SELLER")))
                      .requestAttr("gateway.sellerId", sellerId.toString()))
              .andExpect(status().isOk())
              .andReturn();

      String responseBody = pollResult.getResponse().getContentAsString();
      JsonNode json = objectMapper.readTree(responseBody);
      String status = json.get("status").asText();

      System.out.println("=== E2E REAL (c2f2): Poll " + i + "/20 - status=" + status + " ===");

      if ("APPROVED".equals(status) || "REJECTED".equals(status)) {
        System.out.println("=== E2E REAL (c2f2): Resultado final: " + status + " ===");
        System.out.println("=== E2E REAL (c2f2): Response: " + responseBody + " ===");
        foundFinalStatus = true;
        break;
      }
    }

    org.assertj.core.api.Assertions.assertThat(foundFinalStatus)
        .as("El microservicio debió devolver un estado final (APPROVED o REJECTED)")
        .isTrue();
  }

  // ==================== Happy Path: misma imagen = APPROVED ====================

  @Test
  void e2e_real_happyPath_cedula3Foto10_shouldApprove() throws Exception {
    // Subir cedula (misma imagen para cedula y selfie = match garantizado)
    MockMultipartFile cedulaFile = createMultipartFile(CEDULA3_JPG, "cedula3.jpeg");
    mockMvc
        .perform(
            multipart(String.format(BASE_PATH, sellerId) + "/document")
                .file(cedulaFile)
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("DOCUMENT_UPLOADED"));

    // Subir selfie (misma imagen)
    MockMultipartFile selfieFile = createMultipartFile(FOTO10_JPG, "foto10.jpeg");
    mockMvc
        .perform(
            multipart(String.format(BASE_PATH, sellerId) + "/selfie")
                .file(selfieFile)
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SELFIE_UPLOADED"));

    // Validar
    MvcResult validateResult =
        mockMvc
            .perform(
                post(String.format(BASE_PATH, sellerId) + "/validate")
                    .with(
                        org.springframework.security.test.web.servlet.request
                            .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                            .authorities(
                                new org.springframework.security.core.authority
                                    .SimpleGrantedAuthority("ROLE_SELLER")))
                    .requestAttr("gateway.sellerId", sellerId.toString()))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.status").value("PROCESSING"))
            .andExpect(jsonPath("$.taskId").isNotEmpty())
            .andReturn();

    String taskId =
        objectMapper
            .readTree(validateResult.getResponse().getContentAsByteArray())
            .get("taskId")
            .asText();
    System.out.println("=== E2E REAL HAPPY PATH: taskId=" + taskId + " ===");

    // Polling
    boolean foundFinalStatus = false;
    for (int i = 1; i <= 20; i++) {
      Thread.sleep(5000);
      MvcResult pollResult =
          mockMvc
              .perform(
                  get(String.format(BASE_PATH, sellerId))
                      .with(
                          org.springframework.security.test.web.servlet.request
                              .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                              .authorities(
                                  new org.springframework.security.core.authority
                                      .SimpleGrantedAuthority("ROLE_SELLER")))
                      .requestAttr("gateway.sellerId", sellerId.toString()))
              .andExpect(status().isOk())
              .andReturn();

      String responseBody = pollResult.getResponse().getContentAsString();
      JsonNode json = objectMapper.readTree(responseBody);
      String status = json.get("status").asText();

      System.out.println("=== E2E REAL HAPPY PATH: Poll " + i + "/20 - status=" + status + " ===");

      if ("APPROVED".equals(status) || "REJECTED".equals(status)) {
        System.out.println("=== E2E REAL HAPPY PATH: Resultado final: " + status + " ===");
        System.out.println("=== E2E REAL HAPPY PATH: Response: " + responseBody + " ===");
        foundFinalStatus = true;

        org.assertj.core.api.Assertions.assertThat(status)
            .as("Con cedula3 + foto10, el resultado debe ser APPROVED")
            .isEqualTo("APPROVED");
        break;
      }
    }

    org.assertj.core.api.Assertions.assertThat(foundFinalStatus)
        .as("El microservicio debió devolver un estado final")
        .isTrue();
  }

  // ==================== Helpers ====================

  private MockMultipartFile createMultipartFile(String filePath, String filename)
      throws java.io.IOException {
    byte[] content = Files.readAllBytes(Path.of(filePath));
    return new MockMultipartFile("file", filename, "image/jpeg", content);
  }

  private SellerEntity createSellerEntity(UUID id) {
    SellerEntity seller = new SellerEntity();
    seller.setId(id);
    seller.setTypeTrade("NATURAL");
    seller.setTypeDni("CC");
    seller.setDniNumber(String.valueOf(DNI_COUNTER.getAndIncrement()));
    seller.setTradeName("Tienda Real Prueba");
    seller.setFullname("Vendedor Real Prueba");
    seller.setIsActive(true);
    seller.setIsVerified(false);
    seller.setCreatedAt(Timestamp.from(Instant.now()));
    return seller;
  }

  private SellerContactEntity createContactEntity(UUID sellerId, SellerEntity seller) {
    SellerContactEntity contact = new SellerContactEntity();
    contact.setId(sellerId);
    contact.setSeller(seller);
    contact.setEmail(
        "e2e-real-" + sellerId.toString().replace("-", "").substring(0, 8) + "@test.com");
    contact.setPhoneNumber("3009876543");
    contact.setTradeAddress("Calle E2E Real #456");
    contact.setTradeDepartment("Bogota");
    contact.setTradeCity("Bogota");
    return contact;
  }
}
