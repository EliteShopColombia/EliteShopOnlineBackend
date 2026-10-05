package com.eliteshop.colombia.seller.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.eliteshop.colombia.seller.application.usecase.VerifySellerUseCase;
import com.eliteshop.colombia.seller.domain.event.SellerVerificationCompletedEvent;
import com.eliteshop.colombia.seller.domain.exception.SellerNotFoundException;
import com.eliteshop.colombia.seller.domain.model.*;
import com.eliteshop.colombia.seller.domain.model.verification.*;
import com.eliteshop.colombia.seller.domain.repository.SellerRepository;
import com.eliteshop.colombia.seller.infrastructure.adapter.FaceMatcherAdapter;
import com.eliteshop.colombia.seller.infrastructure.adapter.MinIOAdapter;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class VerifySellerUseCaseTest {

  @Mock private SellerVerificationRepository verificationRepository;
  @Mock private SellerRepository sellerRepository;
  @Mock private MinIOAdapter minIOAdapter;
  @Mock private FaceMatcherAdapter faceMatcherAdapter;
  @Mock private ApplicationEventPublisher eventPublisher;

  private VerifySellerUseCase useCase;

  private UUID sellerId;

  @BeforeEach
  void setUp() {
    useCase =
        new VerifySellerUseCase(
            verificationRepository,
            sellerRepository,
            minIOAdapter,
            faceMatcherAdapter,
            eventPublisher);
    sellerId = UUID.randomUUID();
  }

  // ==================== uploadDocument ====================

  @Test
  void uploadDocument_shouldCreateNewVerificationWhenNoneExists() {
    InputStream stream = new ByteArrayInputStream("doc".getBytes());
    Seller seller = buildSeller(sellerId);

    when(sellerRepository.findById(any(SellerId.class))).thenReturn(Optional.of(seller));
    when(verificationRepository.findBySellerId(sellerId)).thenReturn(Optional.empty());
    when(minIOAdapter.uploadDocument(eq(sellerId.toString()), anyString(), any(InputStream.class)))
        .thenReturn("sellers/" + sellerId + "/document.jpg");
    when(verificationRepository.save(any(SellerVerification.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    SellerVerification result = useCase.uploadDocument(sellerId, "doc.jpg", stream);

    assertThat(result.getStatus().getValue()).isEqualTo("DOCUMENT_UPLOADED");
    assertThat(result.getDocumentNumber().getValue()).isEqualTo("12345678");
    verify(verificationRepository).save(any(SellerVerification.class));
  }

  @Test
  void uploadDocument_shouldThrowWhenSellerNotFound() {
    InputStream stream = new ByteArrayInputStream("doc".getBytes());

    when(sellerRepository.findById(any(SellerId.class))).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.uploadDocument(sellerId, "doc.jpg", stream))
        .isInstanceOf(SellerNotFoundException.class);
  }

  // ==================== uploadSelfie ====================

  @Test
  void uploadSelfie_shouldUpdateExistingVerification() {
    InputStream stream = new ByteArrayInputStream("selfie".getBytes());
    SellerVerification existing = buildVerification(sellerId, "DOCUMENT_UPLOADED");

    when(verificationRepository.findBySellerId(sellerId)).thenReturn(Optional.of(existing));
    when(minIOAdapter.uploadSelfie(eq(sellerId.toString()), anyString(), any(InputStream.class)))
        .thenReturn("sellers/" + sellerId + "/selfie.jpg");
    when(verificationRepository.save(any(SellerVerification.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    SellerVerification result = useCase.uploadSelfie(sellerId, "selfie.jpg", stream);

    assertThat(result.getStatus().getValue()).isEqualTo("SELFIE_UPLOADED");
  }

  @Test
  void uploadSelfie_shouldThrowWhenNoPendingVerification() {
    InputStream stream = new ByteArrayInputStream("selfie".getBytes());

    when(verificationRepository.findBySellerId(sellerId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.uploadSelfie(sellerId, "selfie.jpg", stream))
        .isInstanceOf(
            com.eliteshop.colombia.seller.domain.exception.SellerVerificationException.class);
  }

  // ==================== submitVerification ====================

  @Test
  void submitVerification_shouldEnqueueAndReturnTaskId() {
    SellerVerification existing = buildVerification(sellerId, "SELFIE_UPLOADED");
    String expectedTaskId = "task-" + UUID.randomUUID();

    when(verificationRepository.findBySellerId(sellerId)).thenReturn(Optional.of(existing));
    when(faceMatcherAdapter.submitVerification(anyString(), anyString(), eq(sellerId.toString())))
        .thenReturn(expectedTaskId);
    when(verificationRepository.save(any(SellerVerification.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    SellerVerification result = useCase.submitVerification(sellerId);

    assertThat(result.getStatus().getValue()).isEqualTo("PROCESSING");
    assertThat(result.getTaskId()).isNotNull();
    assertThat(result.getTaskId().getValue()).isEqualTo(expectedTaskId);
    verify(faceMatcherAdapter)
        .submitVerification(
            eq("sellers/" + sellerId + "/selfie.jpg"),
            eq("sellers/" + sellerId + "/document.jpg"),
            eq(sellerId.toString()));
    verify(verificationRepository).save(any(SellerVerification.class));
  }

  @Test
  void submitVerification_shouldThrowWhenNoVerificationExists() {
    when(verificationRepository.findBySellerId(sellerId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.submitVerification(sellerId))
        .isInstanceOf(
            com.eliteshop.colombia.seller.domain.exception.SellerVerificationException.class);
  }

  @Test
  void submitVerification_shouldThrowWhenStatusIsNotSelfieUploaded() {
    SellerVerification existing = buildVerification(sellerId, "PENDING");

    when(verificationRepository.findBySellerId(sellerId)).thenReturn(Optional.of(existing));

    assertThatThrownBy(() -> useCase.submitVerification(sellerId))
        .isInstanceOf(
            com.eliteshop.colombia.seller.domain.exception.SellerVerificationException.class);
  }

  @Test
  void submitVerification_shouldThrowWhenMicroserviceFails() {
    SellerVerification existing = buildVerification(sellerId, "SELFIE_UPLOADED");

    when(verificationRepository.findBySellerId(sellerId)).thenReturn(Optional.of(existing));
    when(faceMatcherAdapter.submitVerification(anyString(), anyString(), eq(sellerId.toString())))
        .thenThrow(
            new FaceMatcherAdapter.VerificationSubmitException(
                "Error de comunicación con el microservicio"));

    assertThatThrownBy(() -> useCase.submitVerification(sellerId))
        .isInstanceOf(FaceMatcherAdapter.VerificationSubmitException.class)
        .hasMessageContaining("Error de comunicación");
  }

  // ==================== pollVerificationResult ====================

  @Test
  void pollVerificationResult_shouldApproveAndStoreDetailedMetrics() {
    String taskId = "task-123";
    SellerVerification existing = buildVerificationWithTaskId(sellerId, "PROCESSING", taskId);

    when(verificationRepository.findBySellerId(sellerId)).thenReturn(Optional.of(existing));
    when(faceMatcherAdapter.getVerificationResult(taskId))
        .thenReturn(
            FaceMatcherAdapter.VerificationResult.of(
                "approved", 0.92, 0.98, 0.95, "12345678", "Juan Perez", "Identidad verificada"));
    when(verificationRepository.save(any(SellerVerification.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    SellerVerification result = useCase.pollVerificationResult(sellerId);

    assertThat(result.getStatus().getValue()).isEqualTo("APPROVED");
    assertThat(result.getConfidenceScore().getValue()).isEqualTo(0.92);
    assertThat(result.getLivenessConfidence()).isNotNull();
    assertThat(result.getLivenessConfidence().getValue()).isEqualTo(0.98);
    assertThat(result.getAntispoofScore()).isNotNull();
    assertThat(result.getAntispoofScore().getValue()).isEqualTo(0.95);
    assertThat(result.getOcrCedulaNumber()).isNotNull();
    assertThat(result.getOcrCedulaNumber().getValue()).isEqualTo("12345678");
    assertThat(result.getOcrCedulaName()).isNotNull();
    assertThat(result.getOcrCedulaName().getValue()).isEqualTo("Juan Perez");
    verify(verificationRepository).save(any(SellerVerification.class));
  }

  @Test
  void pollVerificationResult_shouldRejectAndStoreMetrics() {
    String taskId = "task-456";
    SellerVerification existing = buildVerificationWithTaskId(sellerId, "PROCESSING", taskId);

    when(verificationRepository.findBySellerId(sellerId)).thenReturn(Optional.of(existing));
    when(faceMatcherAdapter.getVerificationResult(taskId))
        .thenReturn(
            FaceMatcherAdapter.VerificationResult.of(
                "rejected", 0.30, 0.45, 0.20, "12345678", "Juan Perez", "Rostro no coincide"));
    when(verificationRepository.save(any(SellerVerification.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    SellerVerification result = useCase.pollVerificationResult(sellerId);

    assertThat(result.getStatus().getValue()).isEqualTo("REJECTED");
    assertThat(result.getConfidenceScore().getValue()).isEqualTo(0.30);
    assertThat(result.getLivenessConfidence().getValue()).isEqualTo(0.45);
    assertThat(result.getAntispoofScore().getValue()).isEqualTo(0.20);
    assertThat(result.getRejectionReason().getValue()).isEqualTo("Rostro no coincide");
  }

  @Test
  void pollVerificationResult_shouldReturnVerificationWhenStillQueued() {
    String taskId = "task-789";
    SellerVerification existing = buildVerificationWithTaskId(sellerId, "PROCESSING", taskId);

    when(verificationRepository.findBySellerId(sellerId)).thenReturn(Optional.of(existing));
    when(faceMatcherAdapter.getVerificationResult(taskId))
        .thenReturn(
            FaceMatcherAdapter.VerificationResult.of(
                "queued", null, null, null, null, null, "En cola de procesamiento"));

    SellerVerification result = useCase.pollVerificationResult(sellerId);

    assertThat(result.getStatus().getValue()).isEqualTo("PROCESSING");
    verify(verificationRepository, never()).save(any(SellerVerification.class));
  }

  @Test
  void pollVerificationResult_shouldReturnVerificationWhenStillProcessing() {
    String taskId = "task-abc";
    SellerVerification existing = buildVerificationWithTaskId(sellerId, "PROCESSING", taskId);

    when(verificationRepository.findBySellerId(sellerId)).thenReturn(Optional.of(existing));
    when(faceMatcherAdapter.getVerificationResult(taskId))
        .thenReturn(
            FaceMatcherAdapter.VerificationResult.of(
                "processing", null, null, null, null, null, "Procesando"));

    SellerVerification result = useCase.pollVerificationResult(sellerId);

    assertThat(result.getStatus().getValue()).isEqualTo("PROCESSING");
  }

  @Test
  void pollVerificationResult_shouldThrowWhenNoTaskId() {
    SellerVerification existing = buildVerification(sellerId, "SELFIE_UPLOADED");

    when(verificationRepository.findBySellerId(sellerId)).thenReturn(Optional.of(existing));

    assertThatThrownBy(() -> useCase.pollVerificationResult(sellerId))
        .isInstanceOf(
            com.eliteshop.colombia.seller.domain.exception.SellerVerificationException.class)
        .hasMessageContaining("No hay tarea de verificación encolada");
  }

  @Test
  void pollVerificationResult_shouldPublishEventOnApproval() {
    String taskId = "task-evt";
    SellerVerification existing = buildVerificationWithTaskId(sellerId, "PROCESSING", taskId);

    when(verificationRepository.findBySellerId(sellerId)).thenReturn(Optional.of(existing));
    when(faceMatcherAdapter.getVerificationResult(taskId))
        .thenReturn(
            FaceMatcherAdapter.VerificationResult.of(
                "approved", 0.88, 0.95, 0.90, "87654321", "Maria Lopez", "OK"));
    when(verificationRepository.save(any(SellerVerification.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    useCase.pollVerificationResult(sellerId);

    ArgumentCaptor<SellerVerificationCompletedEvent> captor =
        ArgumentCaptor.forClass(SellerVerificationCompletedEvent.class);
    verify(eventPublisher).publishEvent(captor.capture());

    SellerVerificationCompletedEvent event = captor.getValue();
    assertThat(event.sellerId()).isEqualTo(sellerId);
    assertThat(event.verified()).isTrue();
    assertThat(event.confidence()).isEqualTo(0.88);
  }

  @Test
  void pollVerificationResult_shouldPublishEventOnRejection() {
    String taskId = "task-rej";
    SellerVerification existing = buildVerificationWithTaskId(sellerId, "PROCESSING", taskId);

    when(verificationRepository.findBySellerId(sellerId)).thenReturn(Optional.of(existing));
    when(faceMatcherAdapter.getVerificationResult(taskId))
        .thenReturn(
            FaceMatcherAdapter.VerificationResult.of(
                "rejected", 0.15, 0.10, 0.05, null, null, "Cara no encontrada"));
    when(verificationRepository.save(any(SellerVerification.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    useCase.pollVerificationResult(sellerId);

    ArgumentCaptor<SellerVerificationCompletedEvent> captor =
        ArgumentCaptor.forClass(SellerVerificationCompletedEvent.class);
    verify(eventPublisher).publishEvent(captor.capture());

    SellerVerificationCompletedEvent event = captor.getValue();
    assertThat(event.sellerId()).isEqualTo(sellerId);
    assertThat(event.verified()).isFalse();
    assertThat(event.confidence()).isEqualTo(0.15);
    assertThat(event.message()).isEqualTo("Cara no encontrada");
  }

  @Test
  void pollVerificationResult_shouldThrowWhenMicroserviceReturnsNullStatus() {
    String taskId = "task-null";
    SellerVerification existing = buildVerificationWithTaskId(sellerId, "PROCESSING", taskId);

    when(verificationRepository.findBySellerId(sellerId)).thenReturn(Optional.of(existing));
    when(faceMatcherAdapter.getVerificationResult(taskId))
        .thenReturn(
            FaceMatcherAdapter.VerificationResult.of(null, null, null, null, null, null, null));

    assertThatThrownBy(() -> useCase.pollVerificationResult(sellerId))
        .isInstanceOf(
            com.eliteshop.colombia.seller.domain.exception.SellerVerificationException.class)
        .hasMessageContaining("estado nulo");
  }

  @Test
  void pollVerificationResult_shouldThrowOnUnknownStatus() {
    String taskId = "task-unknown";
    SellerVerification existing = buildVerificationWithTaskId(sellerId, "PROCESSING", taskId);

    when(verificationRepository.findBySellerId(sellerId)).thenReturn(Optional.of(existing));
    when(faceMatcherAdapter.getVerificationResult(taskId))
        .thenReturn(
            FaceMatcherAdapter.VerificationResult.of(
                "ERROR_UNEXPECTED", null, null, null, null, null, null));

    assertThatThrownBy(() -> useCase.pollVerificationResult(sellerId))
        .isInstanceOf(
            com.eliteshop.colombia.seller.domain.exception.SellerVerificationException.class)
        .hasMessageContaining("Estado desconocido");
  }

  // ==================== pollWithRetries ====================

  @Test
  void pollWithRetries_shouldReturnNullWhenNoTaskId() {
    SellerVerification existing = buildVerification(sellerId, "SELFIE_UPLOADED");

    when(verificationRepository.findBySellerId(sellerId)).thenReturn(Optional.of(existing));

    SellerVerification result = useCase.pollWithRetries(sellerId);

    assertThat(result).isNull();
  }

  @Test
  void pollWithRetries_shouldReturnNullWhenVerificationNotFound() {
    when(verificationRepository.findBySellerId(sellerId)).thenReturn(Optional.empty());

    SellerVerification result = useCase.pollWithRetries(sellerId);

    assertThat(result).isNull();
  }

  // ==================== validate (legacy) ====================

  @Test
  void validate_shouldApproveAndSetConfidenceScoreWhenMatchSucceeds() {
    SellerVerification existing = buildVerification(sellerId, "SELFIE_UPLOADED");

    when(verificationRepository.findBySellerId(sellerId)).thenReturn(Optional.of(existing));
    when(faceMatcherAdapter.match(anyString(), anyString()))
        .thenReturn(new FaceMatcherAdapter.FaceMatchResult(true, 0.85, true, true, "Match OK"));
    when(verificationRepository.save(any(SellerVerification.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    SellerVerification result = useCase.validate(sellerId);

    assertThat(result.getStatus().getValue()).isEqualTo("APPROVED");
    assertThat(result.getConfidenceScore()).isNotNull();
    assertThat(result.getConfidenceScore().getValue()).isEqualTo(0.85);
    assertThat(result.getRejectionReason()).isNull();
  }

  @Test
  void validate_shouldRejectAndSetConfidenceScoreWhenMatchFails() {
    SellerVerification existing = buildVerification(sellerId, "SELFIE_UPLOADED");

    when(verificationRepository.findBySellerId(sellerId)).thenReturn(Optional.of(existing));
    when(faceMatcherAdapter.match(anyString(), anyString()))
        .thenReturn(
            new FaceMatcherAdapter.FaceMatchResult(false, 0.35, true, false, "Face mismatch"));
    when(verificationRepository.save(any(SellerVerification.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    SellerVerification result = useCase.validate(sellerId);

    assertThat(result.getStatus().getValue()).isEqualTo("REJECTED");
    assertThat(result.getConfidenceScore()).isNotNull();
    assertThat(result.getConfidenceScore().getValue()).isEqualTo(0.35);
    assertThat(result.getRejectionReason()).isNotNull();
    assertThat(result.getRejectionReason().getValue()).isEqualTo("Face mismatch");
  }

  @Test
  void validate_shouldRejectWhenConfidenceBelowThreshold() {
    SellerVerification existing = buildVerification(sellerId, "SELFIE_UPLOADED");

    when(verificationRepository.findBySellerId(sellerId)).thenReturn(Optional.of(existing));
    when(faceMatcherAdapter.match(anyString(), anyString()))
        .thenReturn(
            new FaceMatcherAdapter.FaceMatchResult(true, 0.45, true, true, "Low confidence"));
    when(verificationRepository.save(any(SellerVerification.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    SellerVerification result = useCase.validate(sellerId);

    assertThat(result.getStatus().getValue()).isEqualTo("REJECTED");
    assertThat(result.getConfidenceScore().getValue()).isEqualTo(0.45);
    assertThat(result.getRejectionReason().getValue()).isEqualTo("Low confidence");
  }

  @Test
  void validate_shouldPublishEventWithCorrectDataOnRejection() {
    SellerVerification existing = buildVerification(sellerId, "SELFIE_UPLOADED");

    when(verificationRepository.findBySellerId(sellerId)).thenReturn(Optional.of(existing));
    when(faceMatcherAdapter.match(anyString(), anyString()))
        .thenReturn(
            new FaceMatcherAdapter.FaceMatchResult(false, 0.20, false, false, "No face found"));
    when(verificationRepository.save(any(SellerVerification.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    useCase.validate(sellerId);

    ArgumentCaptor<SellerVerificationCompletedEvent> captor =
        ArgumentCaptor.forClass(SellerVerificationCompletedEvent.class);
    verify(eventPublisher).publishEvent(captor.capture());

    SellerVerificationCompletedEvent event = captor.getValue();
    assertThat(event.sellerId()).isEqualTo(sellerId);
    assertThat(event.verified()).isFalse();
    assertThat(event.confidence()).isEqualTo(0.20);
    assertThat(event.message()).isEqualTo("No face found");
  }

  @Test
  void validate_shouldThrowWhenNoVerificationExists() {
    when(verificationRepository.findBySellerId(sellerId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.validate(sellerId))
        .isInstanceOf(
            com.eliteshop.colombia.seller.domain.exception.SellerVerificationException.class);
  }

  @Test
  void validate_shouldThrowWhenStatusIsNotSelfieUploaded() {
    SellerVerification existing = buildVerification(sellerId, "PENDING");

    when(verificationRepository.findBySellerId(sellerId)).thenReturn(Optional.of(existing));

    assertThatThrownBy(() -> useCase.validate(sellerId))
        .isInstanceOf(
            com.eliteshop.colombia.seller.domain.exception.SellerVerificationException.class);
  }

  // ==================== getStatus ====================

  @Test
  void getStatus_shouldReturnVerification() {
    SellerVerification existing = buildVerification(sellerId, "PENDING");

    when(verificationRepository.findBySellerId(sellerId)).thenReturn(Optional.of(existing));

    SellerVerification result = useCase.getStatus(sellerId);

    assertThat(result).isNotNull();
    assertThat(result.getSellerId().getValue()).isEqualTo(sellerId);
  }

  @Test
  void getStatus_shouldThrowWhenNotFound() {
    when(verificationRepository.findBySellerId(sellerId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.getStatus(sellerId))
        .isInstanceOf(SellerNotFoundException.class);
  }

  // ==================== Helpers ====================

  private SellerVerification buildVerification(UUID sellerId, String status) {
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
        null,
        null,
        null,
        null,
        null);
  }

  private SellerVerification buildVerificationWithTaskId(
      UUID sellerId, String status, String taskId) {
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
        new SellerVerificationTaskId(taskId),
        null,
        null,
        null,
        null);
  }

  private Seller buildSeller(UUID id) {
    SellerContact contact =
        new SellerContact(
            new SellerEmail("seller@test.com"),
            new SellerPhoneNumber("3001234567"),
            new SellerTradeAddress("Calle 1"),
            new SellerTradeDepartment("Bogota"),
            new SellerTradeCity("Bogota"));

    return new Seller(
        new SellerId(id),
        SellerTypeTrade.NATURAL,
        SellerTypeDni.CC,
        new SellerDniNumber("12345678"),
        new SellerTradeName("Mi Tienda"),
        new SellerFullname("Juan Vendedor"),
        new SellerIsActive(true),
        new SellerIsVerified(false),
        new SellerCreatedAt(Timestamp.from(Instant.now())),
        null,
        null,
        contact,
        null);
  }
}
