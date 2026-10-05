package com.eliteshop.colombia.seller.infrastructure.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.eliteshop.colombia.seller.infrastructure.adapter.FaceMatcherAdapter;
import com.eliteshop.colombia.seller.infrastructure.adapter.MinIOAdapter;
import com.eliteshop.colombia.seller.infrastructure.persistence.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * End-to-end tests para el flujo de verificación de vendedores.
 *
 * <p>Usa H2 real (profile test), MockMvc completo, y fotos reales de
 * /home/arch-tarok/Documentos/Prueba/. Los servicios externos (MinIO y FaceMatcher) se mockean a
 * nivel de bean para aislar las dependencias de infraestructura externa.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@org.junit.jupiter.api.Tag("e2e")
class SellerVerificationE2ETest {


  /** Salta el caso si la foto no existe en disco, para no romper la suite. */
  private static byte[] readOrSkip(String path) throws IOException {
    Assumptions.assumeTrue(
        Files.exists(Path.of(path)), "Falta el archivo de prueba: " + path);
    return Files.readAllBytes(Path.of(path));
  }

  private static final String BASE_PATH = "/api/v1/sellers/%s/verification";
  private static final String CEDULA_JPG = "/home/arch-tarok/Documentos/Prueba/Cedula.jpeg";
  private static final String CEDULA2_JPG = "/home/arch-tarok/Documentos/Prueba/Cedula2.jpeg";
  private static final String CEDULA3_JPG = "/home/arch-tarok/Documentos/Prueba/Cedula3.jpeg";
  private static final String CEDULA4_JPG = "/home/arch-tarok/Documentos/Prueba/Cedula4.jpeg";
  private static final String FOTO_JPG = "/home/arch-tarok/Documentos/Prueba/Foto.jpeg";
  private static final String FOTO2_JPG = "/home/arch-tarok/Documentos/Prueba/Foto2.jpeg";
  private static final String FOTO3_JPG = "/home/arch-tarok/Documentos/Prueba/Foto3.jpeg";
  private static final String FOTO4_JPG = "/home/arch-tarok/Documentos/Prueba/Foto4.jpeg";

  @Autowired private MockMvc mockMvc;
  @Autowired private SellerJpaRepository sellerJpaRepository;
  @Autowired private SellerVerificationJpaRepository verificationJpaRepository;

  @MockitoBean private MinIOAdapter minIOAdapter;
  @MockitoBean private FaceMatcherAdapter faceMatcherAdapter;

  private UUID sellerId;
  private static final AtomicInteger DNI_COUNTER = new AtomicInteger(10000000);

  @BeforeEach
  void setUp() {
    sellerId = UUID.randomUUID();
    SellerEntity seller = createSellerEntity(sellerId);
    SellerContactEntity contact = createContactEntity(sellerId, seller);
    seller.setContact(contact);
    sellerJpaRepository.save(seller);

    when(minIOAdapter.uploadDocument(anyString(), anyString(), org.mockito.ArgumentMatchers.any()))
        .thenReturn("sellers/" + sellerId + "/document.jpg");
    when(minIOAdapter.uploadSelfie(anyString(), anyString(), org.mockito.ArgumentMatchers.any()))
        .thenReturn("sellers/" + sellerId + "/selfie.jpg");
  }

  @AfterEach
  void cleanUp() {
    verificationJpaRepository.findBySellerId(sellerId).ifPresent(verificationJpaRepository::delete);
    sellerJpaRepository.deleteById(sellerId);
  }

  // ==================== Happy Path: Flujo completo ====================

  @Test
  void e2e_fullFlow_uploadDocUploadSelfieValidatePollApproved() throws Exception {
    // Paso 1: Subir cedula
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

    // Paso 2: Subir selfie
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
    String taskId = "task-e2e-" + UUID.randomUUID();
    when(faceMatcherAdapter.submitVerification(anyString(), anyString(), anyString()))
        .thenReturn(taskId);

    mockMvc
        .perform(
            post(String.format(BASE_PATH, sellerId) + "/validate")
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.status").value("PROCESSING"))
        .andExpect(jsonPath("$.taskId").value(taskId));

    // Paso 4: Consultar estado (el microservicio devuelve approved con metricas)
    when(faceMatcherAdapter.getVerificationResult(taskId))
        .thenReturn(
            FaceMatcherAdapter.VerificationResult.of(
                "approved", 0.92, 0.98, 0.95, "12345678", "Juan Perez", "Identidad verificada"));

    mockMvc
        .perform(
            get(String.format(BASE_PATH, sellerId))
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("APPROVED"))
        .andExpect(jsonPath("$.confidenceScore").value(0.92))
        .andExpect(jsonPath("$.livenessConfidence").value(0.98))
        .andExpect(jsonPath("$.antispoofScore").value(0.95))
        .andExpect(jsonPath("$.ocrCedulaNumber").value("12345678"))
        .andExpect(jsonPath("$.ocrCedulaName").value("Juan Perez"));
  }

  // ==================== Flujo completo con RECHAZO ====================

  @Test
  void e2e_fullFlow_validateRejected() throws Exception {
    // Subir documentos
    uploadDocumentAndSelfie(CEDULA2_JPG, FOTO2_JPG);

    // Validar
    String taskId = "task-rejected-" + UUID.randomUUID();
    when(faceMatcherAdapter.submitVerification(anyString(), anyString(), anyString()))
        .thenReturn(taskId);

    mockMvc
        .perform(
            post(String.format(BASE_PATH, sellerId) + "/validate")
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isAccepted());

    // Polling: microservicio responde rejected
    when(faceMatcherAdapter.getVerificationResult(taskId))
        .thenReturn(
            FaceMatcherAdapter.VerificationResult.of(
                "rejected", 0.25, 0.30, 0.15, null, null, "Rostro no coincide con la cedula"));

    mockMvc
        .perform(
            get(String.format(BASE_PATH, sellerId))
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("REJECTED"))
        .andExpect(jsonPath("$.confidenceScore").value(0.25))
        .andExpect(jsonPath("$.rejectionReason").value("Rostro no coincide con la cedula"));
  }

  // ==================== Upload con diferentes fotos ====================

  @Test
  void e2e_uploadDocument_withCedula3() throws Exception {
    MockMultipartFile file = createMultipartFile(CEDULA3_JPG, "cedula3.jpeg");
    mockMvc
        .perform(
            multipart(String.format(BASE_PATH, sellerId) + "/document")
                .file(file)
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("DOCUMENT_UPLOADED"));
  }

  @Test
  void e2e_uploadDocument_withCedula4() throws Exception {
    MockMultipartFile file = createMultipartFile(CEDULA4_JPG, "cedula4.jpeg");
    mockMvc
        .perform(
            multipart(String.format(BASE_PATH, sellerId) + "/document")
                .file(file)
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("DOCUMENT_UPLOADED"));
  }

  @Test
  void e2e_uploadSelfie_withFoto3() throws Exception {
    uploadDocumentOnly(CEDULA_JPG);

    MockMultipartFile file = createMultipartFile(FOTO3_JPG, "foto3.jpeg");
    mockMvc
        .perform(
            multipart(String.format(BASE_PATH, sellerId) + "/selfie")
                .file(file)
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SELFIE_UPLOADED"));
  }

  @Test
  void e2e_uploadSelfie_withFoto4() throws Exception {
    uploadDocumentOnly(CEDULA4_JPG);

    MockMultipartFile file = createMultipartFile(FOTO4_JPG, "foto4.jpeg");
    mockMvc
        .perform(
            multipart(String.format(BASE_PATH, sellerId) + "/selfie")
                .file(file)
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SELFIE_UPLOADED"));
  }

  // ==================== Validar con diferentes pares doc+selfie ====================

  @Test
  void e2e_validate_withCedula2AndFoto3() throws Exception {
    uploadDocumentAndSelfie(CEDULA2_JPG, FOTO3_JPG);

    String taskId = "task-c2f3-" + UUID.randomUUID();
    when(faceMatcherAdapter.submitVerification(anyString(), anyString(), anyString()))
        .thenReturn(taskId);

    mockMvc
        .perform(
            post(String.format(BASE_PATH, sellerId) + "/validate")
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.taskId").value(taskId));
  }

  @Test
  void e2e_validate_withCedula4AndFoto2() throws Exception {
    uploadDocumentAndSelfie(CEDULA4_JPG, FOTO2_JPG);

    String taskId = "task-c4f2-" + UUID.randomUUID();
    when(faceMatcherAdapter.submitVerification(anyString(), anyString(), anyString()))
        .thenReturn(taskId);

    mockMvc
        .perform(
            post(String.format(BASE_PATH, sellerId) + "/validate")
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.taskId").value(taskId));
  }

  // ==================== Polling con estados intermedios ====================

  @Test
  void e2e_polling_queuedThenApproved() throws Exception {
    uploadDocumentAndSelfie(CEDULA_JPG, FOTO_JPG);

    String taskId = "task-queued-" + UUID.randomUUID();
    when(faceMatcherAdapter.submitVerification(anyString(), anyString(), anyString()))
        .thenReturn(taskId);

    mockMvc
        .perform(
            post(String.format(BASE_PATH, sellerId) + "/validate")
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isAccepted());

    // Primera consulta: queued
    when(faceMatcherAdapter.getVerificationResult(taskId))
        .thenReturn(
            FaceMatcherAdapter.VerificationResult.of(
                "queued", null, null, null, null, null, "En cola"));

    mockMvc
        .perform(
            get(String.format(BASE_PATH, sellerId))
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("PROCESSING"));

    // Segunda consulta: approved
    when(faceMatcherAdapter.getVerificationResult(taskId))
        .thenReturn(
            FaceMatcherAdapter.VerificationResult.of(
                "approved", 0.88, 0.95, 0.90, "87654321", "Maria Lopez", "OK"));

    mockMvc
        .perform(
            get(String.format(BASE_PATH, sellerId))
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("APPROVED"))
        .andExpect(jsonPath("$.ocrCedulaNumber").value("87654321"))
        .andExpect(jsonPath("$.ocrCedulaName").value("Maria Lopez"));
  }

  // ==================== Estado ya procesado ====================

  @Test
  void e2e_getStatus_alreadyApproved() throws Exception {
    uploadDocumentAndSelfie(CEDULA_JPG, FOTO_JPG);

    String taskId = "task-done-" + UUID.randomUUID();
    when(faceMatcherAdapter.submitVerification(anyString(), anyString(), anyString()))
        .thenReturn(taskId);

    // Validar y completar
    mockMvc
        .perform(
            post(String.format(BASE_PATH, sellerId) + "/validate")
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isAccepted());

    when(faceMatcherAdapter.getVerificationResult(taskId))
        .thenReturn(
            FaceMatcherAdapter.VerificationResult.of(
                "approved", 0.95, 0.99, 0.97, "11223344", "Pedro Gomez", "Verificado"));

    // Primer get: completa
    mockMvc
        .perform(
            get(String.format(BASE_PATH, sellerId))
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("APPROVED"));

    // Segundo get: sigue aprobado (no vuelve a llamar microservicio)
    mockMvc
        .perform(
            get(String.format(BASE_PATH, sellerId))
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("APPROVED"));
  }

  // ==================== Validar Legacy ====================

  @Test
  void e2e_validateLegacy_shouldStillWork() throws Exception {
    uploadDocumentAndSelfie(CEDULA_JPG, FOTO_JPG);

    when(faceMatcherAdapter.match(anyString(), anyString()))
        .thenReturn(new FaceMatcherAdapter.FaceMatchResult(true, 0.85, true, true, "Match OK"));

    mockMvc
        .perform(
            post(String.format(BASE_PATH, sellerId) + "/validate-legacy")
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.status").value("PROCESSING"));
  }

  // ==================== Errores de validacion ====================

  @Test
  void e2e_uploadDocument_fileEmpty_shouldReturn400() throws Exception {
    MockMultipartFile emptyFile =
        new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

    mockMvc
        .perform(
            multipart(String.format(BASE_PATH, sellerId) + "/document")
                .file(emptyFile)
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isBadRequest());
  }

  @Test
  void e2e_uploadDocument_unsupportedFormat_shouldReturn400() throws Exception {
    MockMultipartFile pdfFile =
        new MockMultipartFile("file", "doc.pdf", "application/pdf", "pdf-content".getBytes());

    mockMvc
        .perform(
            multipart(String.format(BASE_PATH, sellerId) + "/document")
                .file(pdfFile)
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isBadRequest());
  }

  @Test
  void e2e_validate_withoutDocument_shouldReturn400() throws Exception {
    // Sin subir documento ni selfie, intentar validar
    mockMvc
        .perform(
            post(String.format(BASE_PATH, sellerId) + "/validate")
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isBadRequest());
  }

  @Test
  void e2e_validate_onlyDocumentNoSelfie_shouldReturn400() throws Exception {
    uploadDocumentOnly(CEDULA_JPG);

    mockMvc
        .perform(
            post(String.format(BASE_PATH, sellerId) + "/validate")
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isBadRequest());
  }

  @Test
  void e2e_uploadSelfie_withoutDocument_shouldReturn400() throws Exception {
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
        .andExpect(status().isBadRequest());
  }

  // ==================== Seguridad ====================

  @Test
  void e2e_shouldReturn403_whenCustomerRole() throws Exception {
    mockMvc
        .perform(
            post(String.format(BASE_PATH, sellerId) + "/validate")
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user("other-user-id")
                        .authorities(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_CUSTOMER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isForbidden());
  }

  @Test
  void e2e_shouldReturn401_whenNoAuth() throws Exception {
    mockMvc
        .perform(
            post(String.format(BASE_PATH, sellerId) + "/validate")
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isForbidden());
  }

  // ==================== Verificar persistencia en BD ====================

  @Test
  void e2e_verifyPersistence_allFieldsStoredInDatabase() throws Exception {
    uploadDocumentAndSelfie(CEDULA_JPG, FOTO_JPG);

    String taskId = "task-persist-" + UUID.randomUUID();
    when(faceMatcherAdapter.submitVerification(anyString(), anyString(), anyString()))
        .thenReturn(taskId);

    mockMvc
        .perform(
            post(String.format(BASE_PATH, sellerId) + "/validate")
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isAccepted());

    when(faceMatcherAdapter.getVerificationResult(taskId))
        .thenReturn(
            FaceMatcherAdapter.VerificationResult.of(
                "approved", 0.91, 0.97, 0.93, "55667788", "Ana Garcia", "Todo OK"));

    mockMvc
        .perform(
            get(String.format(BASE_PATH, sellerId))
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isOk());

    // Verificar directamente en la BD que todos los campos se guardaron
    var entity = verificationJpaRepository.findBySellerId(sellerId);
    org.assertj.core.api.Assertions.assertThat(entity).isPresent();
    org.assertj.core.api.Assertions.assertThat(entity.get().getStatus()).isEqualTo("APPROVED");
    org.assertj.core.api.Assertions.assertThat(entity.get().getTaskId()).isEqualTo(taskId);
    org.assertj.core.api.Assertions.assertThat(entity.get().getConfidenceScore()).isEqualTo(0.91);
    org.assertj.core.api.Assertions.assertThat(entity.get().getLivenessConfidence())
        .isEqualTo(0.97);
    org.assertj.core.api.Assertions.assertThat(entity.get().getAntispoofScore()).isEqualTo(0.93);
    org.assertj.core.api.Assertions.assertThat(entity.get().getOcrCedulaNumber())
        .isEqualTo("55667788");
    org.assertj.core.api.Assertions.assertThat(entity.get().getOcrCedulaName())
        .isEqualTo("Ana Garcia");
  }

  // ==================== Flujo completo con diferentes combinaciones de fotos ====================

  @Test
  void e2e_fullFlow_withCedula3AndFoto4_approved() throws Exception {
    uploadDocumentAndSelfie(CEDULA3_JPG, FOTO4_JPG);

    String taskId = "task-c3f4-" + UUID.randomUUID();
    when(faceMatcherAdapter.submitVerification(anyString(), anyString(), anyString()))
        .thenReturn(taskId);

    mockMvc
        .perform(
            post(String.format(BASE_PATH, sellerId) + "/validate")
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isAccepted());

    when(faceMatcherAdapter.getVerificationResult(taskId))
        .thenReturn(
            FaceMatcherAdapter.VerificationResult.of(
                "approved", 0.89, 0.96, 0.91, "99887766", "Carlos Ruiz", "Aprobado"));

    mockMvc
        .perform(
            get(String.format(BASE_PATH, sellerId))
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("APPROVED"))
        .andExpect(jsonPath("$.ocrCedulaNumber").value("99887766"))
        .andExpect(jsonPath("$.ocrCedulaName").value("Carlos Ruiz"));
  }

  @Test
  void e2e_fullFlow_withCedula4AndFoto2_rejected() throws Exception {
    uploadDocumentAndSelfie(CEDULA4_JPG, FOTO2_JPG);

    String taskId = "task-c4f2r-" + UUID.randomUUID();
    when(faceMatcherAdapter.submitVerification(anyString(), anyString(), anyString()))
        .thenReturn(taskId);

    mockMvc
        .perform(
            post(String.format(BASE_PATH, sellerId) + "/validate")
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isAccepted());

    when(faceMatcherAdapter.getVerificationResult(taskId))
        .thenReturn(
            FaceMatcherAdapter.VerificationResult.of(
                "rejected", 0.18, 0.22, 0.10, null, null, "Liveness fallido"));

    mockMvc
        .perform(
            get(String.format(BASE_PATH, sellerId))
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("REJECTED"))
        .andExpect(jsonPath("$.rejectionReason").value("Liveness fallido"));
  }

  // ==================== Helpers ====================

  private void uploadDocumentAndSelfie(String cedulaPath, String selfiePath) throws Exception {
    uploadDocumentOnly(cedulaPath);
    MockMultipartFile selfieFile = createMultipartFile(selfiePath, "selfie.jpeg");
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
        .andExpect(status().isOk());
  }

  private void uploadDocumentOnly(String cedulaPath) throws Exception {
    MockMultipartFile file = createMultipartFile(cedulaPath, "cedula.jpeg");
    mockMvc
        .perform(
            multipart(String.format(BASE_PATH, sellerId) + "/document")
                .file(file)
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(sellerId.toString())
                        .authorities(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_SELLER")))
                .requestAttr("gateway.sellerId", sellerId.toString()))
        .andExpect(status().isOk());
  }

  private MockMultipartFile createMultipartFile(String filePath, String filename)
      throws IOException {
    // Las fotos de prueba no están en el repositorio: si falta alguna, se salta
    // el caso en lugar de fallar toda la suite.
    Assumptions.assumeTrue(
        Files.exists(Path.of(filePath)), "Falta el archivo de prueba: " + filePath);
    byte[] content = Files.readAllBytes(Path.of(filePath));
    return new MockMultipartFile("file", filename, "image/jpeg", content);
  }

  private SellerEntity createSellerEntity(UUID id) {
    SellerEntity seller = new SellerEntity();
    seller.setId(id);
    seller.setTypeTrade("NATURAL");
    seller.setTypeDni("CC");
    seller.setDniNumber(String.valueOf(DNI_COUNTER.getAndIncrement()));
    seller.setTradeName("Tienda Test");
    seller.setFullname("Vendedor Test");
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
        "e2e-test-" + sellerId.toString().substring(0, 8).replace("-", "") + "@test.com");
    contact.setPhoneNumber("3001234567");
    contact.setTradeAddress("Calle E2E #123");
    contact.setTradeDepartment("Bogota");
    contact.setTradeCity("Bogota");
    return contact;
  }
}
