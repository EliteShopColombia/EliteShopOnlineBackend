package com.eliteshop.colombia.seller.application.usecase;

import com.eliteshop.colombia.seller.domain.event.SellerVerificationCompletedEvent;
import com.eliteshop.colombia.seller.domain.exception.SellerNotFoundException;
import com.eliteshop.colombia.seller.domain.model.Seller;
import com.eliteshop.colombia.seller.domain.model.SellerId;
import com.eliteshop.colombia.seller.domain.model.verification.*;
import com.eliteshop.colombia.seller.domain.repository.SellerRepository;
import com.eliteshop.colombia.seller.infrastructure.adapter.FaceMatcherAdapter;
import com.eliteshop.colombia.seller.infrastructure.adapter.MinIOAdapter;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

@Slf4j
@RequiredArgsConstructor
public class VerifySellerUseCase {

  private final SellerVerificationRepository repository;
  private final SellerRepository sellerRepository;
  private final MinIOAdapter minIOAdapter;
  private final FaceMatcherAdapter faceMatcherAdapter;
  private final ApplicationEventPublisher eventPublisher;

  // Polling configuration
  private static final int POLL_MAX_ATTEMPTS = 30;
  private static final Duration POLL_INITIAL_DELAY = Duration.ofSeconds(2);
  private static final Duration POLL_BACKOFF_MULTIPLIER = Duration.ofSeconds(1);

  public SellerVerification uploadDocument(UUID sellerId, String filename, InputStream stream) {
    log.info("Subiendo documento de verificación para vendedor sellerId={}", sellerId);
    String objectKey = minIOAdapter.uploadDocument(sellerId.toString(), filename, stream);

    Seller seller =
        sellerRepository
            .findById(new SellerId(sellerId))
            .orElseThrow(
                () -> {
                  log.error("Vendedor no encontrado sellerId={}", sellerId);
                  return new SellerNotFoundException("Vendedor no encontrado");
                });

    SellerVerification verification =
        repository
            .findBySellerId(sellerId)
            .orElseGet(() -> SellerVerification.create(new SellerVerificationSellerId(sellerId)));

    verification =
        verification.withDocumentUploaded(
            new SellerVerificationDocumentMinioKey(objectKey),
            new SellerVerificationDocumentNumber(seller.getDniNumber().getValue()));
    log.info("Documento subido exitosamente para vendedor sellerId={}", sellerId);
    return repository.save(verification);
  }

  public SellerVerification uploadSelfie(UUID sellerId, String filename, InputStream stream) {
    return uploadSelfie(sellerId, filename, stream, java.util.List.of());
  }

  /**
   * Registra la selfie y, opcionalmente, una ráfaga de frames. Con frames el microservicio puede
   * aplicar el análisis temporal de liveness (parpadeo y movimiento); sin ellos solo analiza la
   * textura de la piel de la imagen.
   */
  public SellerVerification uploadSelfie(
      UUID sellerId, String filename, InputStream stream, java.util.List<InputStream> frames) {
    log.info(
        "Subiendo selfie de verificación para vendedor sellerId={} (frames={})",
        sellerId,
        frames.size());
    String objectKey = minIOAdapter.uploadSelfie(sellerId.toString(), filename, stream);

    SellerVerification verification =
        repository
            .findBySellerId(sellerId)
            .orElseThrow(
                () -> {
                  log.error(
                      "No existe verificación pendiente para sellerId={}, suba la cédula primero",
                      sellerId);
                  return new com.eliteshop.colombia.seller.domain.exception
                      .SellerVerificationException("Sube la cédula primero");
                });

    java.util.List<String> frameKeys = new java.util.ArrayList<>();
    for (int i = 0; i < frames.size(); i++) {
      frameKeys.add(minIOAdapter.uploadSelfieFrame(sellerId.toString(), i, frames.get(i)));
    }

    verification =
        verification.withSelfieUploaded(
            new SellerVerificationSelfieMinioKey(objectKey),
            frameKeys.isEmpty() ? null : SellerVerificationFramesMinioKeys.fromList(frameKeys));
    log.info(
        "Selfie subida exitosamente para vendedor sellerId={} (frames guardados={})",
        sellerId,
        frameKeys.size());
    return repository.save(verification);
  }

  /**
   * Envía la tarea de verificación al microservicio y retorna inmediatamente con el task_id.
   * Transiciona el estado a PROCESSING.
   *
   * @param sellerId ID del vendedor
   * @return verification con el task_id asignado y estado PROCESSING
   */
  public SellerVerification submitVerification(UUID sellerId) {
    log.info("Enviando tarea de verificación para vendedor sellerId={}", sellerId);

    SellerVerification verification =
        repository
            .findBySellerId(sellerId)
            .orElseThrow(
                () -> {
                  log.error("No hay verificación pendiente para sellerId={}", sellerId);
                  return new com.eliteshop.colombia.seller.domain.exception
                      .SellerVerificationException("No hay verificación pendiente");
                });

    if (!"SELFIE_UPLOADED".equals(verification.getStatus().getValue())) {
      log.error(
          "Verificación en estado incorrecto para sellerId={}: {}",
          sellerId,
          verification.getStatus().getValue());
      throw new com.eliteshop.colombia.seller.domain.exception.SellerVerificationException(
          "Primero sube la cédula y la selfie");
    }

    String selfieObject =
        verification.getSelfieMinioKey() != null
            ? verification.getSelfieMinioKey().getValue()
            : null;
    String documentObject =
        verification.getDocumentMinioKey() != null
            ? verification.getDocumentMinioKey().getValue()
            : null;

    log.info(
        "Keys de verificación para sellerId={}: selfie={}, document={}",
        sellerId,
        selfieObject,
        documentObject);

    if (selfieObject == null || documentObject == null) {
      log.error(
          "Keys de MinIO nulas para sellerId={}: selfie={}, document={}",
          sellerId,
          selfieObject,
          documentObject);
      throw new com.eliteshop.colombia.seller.domain.exception.SellerVerificationException(
          "Faltan las imágenes de cédula o selfie. Sube ambas imágenes antes de validar.");
    }

    String taskId =
        faceMatcherAdapter.submitVerification(
            selfieObject, documentObject, verification.getFramesMinioKeys(), sellerId.toString());

    verification = verification.withProcessing(new SellerVerificationTaskId(taskId));
    SellerVerification savedVerification = repository.save(verification);

    log.info("Tarea de verificación encolada para sellerId={}, taskId={}", sellerId, taskId);
    return savedVerification;
  }

  /**
   * Consulta el resultado de verificación del microservicio para un task_id dado. Si el resultado
   * es final (approved/rejected), almacena las métricas detalladas y publica el evento.
   *
   * @param sellerId ID del vendedor
   * @return verification actualizada con el resultado final
   */
  public SellerVerification pollVerificationResult(UUID sellerId) {
    log.info("Consultando resultado de verificación para sellerId={}", sellerId);

    SellerVerification verification =
        repository
            .findBySellerId(sellerId)
            .orElseThrow(
                () -> {
                  log.error("No hay verificación para sellerId={}", sellerId);
                  return new com.eliteshop.colombia.seller.domain.exception
                      .SellerVerificationException("No hay verificación para este vendedor");
                });

    if (verification.getTaskId() == null) {
      throw new com.eliteshop.colombia.seller.domain.exception.SellerVerificationException(
          "No hay tarea de verificación encolada para este vendedor");
    }

    FaceMatcherAdapter.VerificationResult result;
    try {
      result = faceMatcherAdapter.getVerificationResult(verification.getTaskId().getValue());
    } catch (FaceMatcherAdapter.VerificationTaskNotFoundException e) {
      return resubmitLostTask(verification, sellerId);
    }

    String remoteStatus = result.status();
    if (remoteStatus == null) {
      throw new com.eliteshop.colombia.seller.domain.exception.SellerVerificationException(
          "El microservicio devolvió un estado nulo para taskId="
              + verification.getTaskId().getValue());
    }

    return switch (remoteStatus.toLowerCase()) {
      case "approved" -> handleApproved(verification, result);
      case "rejected" -> handleRejected(verification, result);
      case "queued", "processing" -> handlePending(verification, remoteStatus);
      case "error" -> handleMicroserviceError(verification, result);
      default ->
          throw new com.eliteshop.colombia.seller.domain.exception.SellerVerificationException(
              "Estado desconocido del microservicio: " + remoteStatus);
    };
  }

  /**
   * El microservicio falló al procesar la tarea (por ejemplo, una excepción interna). Se marca
   * como rechazada con un motivo explícito para que el vendedor pueda reintentar, en vez de
   * dejarlo bloqueado en PROCESSING.
   */
  private SellerVerification handleMicroserviceError(
      SellerVerification verification, FaceMatcherAdapter.VerificationResult result) {
    UUID sellerId = verification.getSellerId().getValue();
    String detalle =
        result.message() != null && !result.message().isBlank()
            ? result.message()
            : "error desconocido";

    String reason =
        "No se pudo completar la verificación por un fallo del servicio de identidad. "
            + "Vuelve a intentarlo en unos minutos. (" + detalle + ")";

    SellerVerification saved =
        repository.save(
            verification.rejected(
                new SellerVerificationConfidenceScore(
                    result.confidence() != null ? result.confidence() : 0.0),
                new SellerVerificationRejectionReason(reason)));

    eventPublisher.publishEvent(
        new SellerVerificationCompletedEvent(
            sellerId, false, 0.0, reason, Instant.now()));

    log.error(
        "Verificación con error del microservicio para sellerId={}, taskId={}: {}",
        sellerId,
        verification.getTaskId() != null ? verification.getTaskId().getValue() : "N/A",
        detalle);

    return saved;
  }

  /**
   * Ejecuta un polling con reintentos hasta obtener un resultado final del microservicio. Diseñado
   * para ejecutarse en un hilo asíncrono (no bloquea el hilo HTTP).
   *
   * @param sellerId ID del vendedor
   * @return verification con el resultado final, o null si se agotaron los reintentos
   */
  public SellerVerification pollWithRetries(UUID sellerId) {
    log.info(
        "Iniciando polling con reintentos para sellerId={}, maxAttempts={}",
        sellerId,
        POLL_MAX_ATTEMPTS);

    for (int attempt = 1; attempt <= POLL_MAX_ATTEMPTS; attempt++) {
      try {
        java.util.Optional<SellerVerification> optVerification =
            repository.findBySellerId(sellerId);
        if (optVerification.isEmpty()) {
          log.warn("No se encontró verificación para sellerId={} en attempt={}", sellerId, attempt);
          return null;
        }

        SellerVerification verification = optVerification.get();

        if (verification.getTaskId() == null) {
          log.error("No hay task_id para sellerId={}", sellerId);
          return null;
        }

        FaceMatcherAdapter.VerificationResult result =
            faceMatcherAdapter.getVerificationResult(verification.getTaskId().getValue());

        String remoteStatus = result.status();
        log.info(
            "Polling attempt {}/{} para sellerId={}: status={}",
            attempt,
            POLL_MAX_ATTEMPTS,
            sellerId,
            remoteStatus);

        if ("approved".equalsIgnoreCase(remoteStatus)
            || "rejected".equalsIgnoreCase(remoteStatus)) {
          return pollVerificationResult(sellerId);
        }

        // Si es queued o processing, esperar antes del siguiente intento
        Duration delay = POLL_INITIAL_DELAY.plus(POLL_BACKOFF_MULTIPLIER.multipliedBy(attempt));
        Thread.sleep(delay.toMillis());
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        log.warn("Polling interrumpido para sellerId={} en attempt={}", sellerId, attempt);
        return null;
      } catch (FaceMatcherAdapter.VerificationSubmitException e) {
        log.warn(
            "Error en polling attempt {}/{} para sellerId={}: {}",
            attempt,
            POLL_MAX_ATTEMPTS,
            sellerId,
            e.getMessage());
        try {
          Thread.sleep(POLL_INITIAL_DELAY.toMillis());
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
          return null;
        }
      }
    }

    log.error("Se agotaron los reintentos de polling para sellerId={}", sellerId);
    return null;
  }

  /**
   * Mantiene la compatibilidad con el flujo legacy (síncrono). Valida la verificación usando el
   * endpoint /match directo sin encolamiento.
   */
  public SellerVerification validate(UUID sellerId) {
    log.info("Validando verificación (legacy) para vendedor sellerId={}", sellerId);
    SellerVerification verification =
        repository
            .findBySellerId(sellerId)
            .orElseThrow(
                () -> {
                  log.error("No hay verificación pendiente para sellerId={}", sellerId);
                  return new com.eliteshop.colombia.seller.domain.exception
                      .SellerVerificationException("No hay verificación pendiente");
                });

    if (!"SELFIE_UPLOADED".equals(verification.getStatus().getValue())) {
      log.error(
          "Verificación en estado incorrecto para sellerId={}: {}",
          sellerId,
          verification.getStatus().getValue());
      throw new com.eliteshop.colombia.seller.domain.exception.SellerVerificationException(
          "Primero sube la cédula y la selfie");
    }

    String selfieObject = verification.getSelfieMinioKey().getValue();
    String documentObject = verification.getDocumentMinioKey().getValue();

    FaceMatcherAdapter.FaceMatchResult result =
        faceMatcherAdapter.match(selfieObject, documentObject);

    boolean isApproved = result.match() && result.confidence() >= 0.6;

    if (isApproved) {
      verification =
          verification.approved(new SellerVerificationConfidenceScore(result.confidence()));
    } else {
      verification =
          verification.rejected(
              new SellerVerificationConfidenceScore(result.confidence()),
              new SellerVerificationRejectionReason(result.message()));
    }

    SellerVerification savedVerification = repository.save(verification);

    eventPublisher.publishEvent(
        new SellerVerificationCompletedEvent(
            sellerId, isApproved, result.confidence(), result.message(), Instant.now()));

    log.info(
        "Verificación completada para sellerId={}, aprobado={}, confianza={}",
        sellerId,
        isApproved,
        result.confidence());
    return savedVerification;
  }

  public SellerVerification getStatus(UUID sellerId) {
    log.info("Consultando estado de verificación para vendedor sellerId={}", sellerId);
    return repository
        .findBySellerId(sellerId)
        .orElseThrow(
            () -> {
              log.error("No hay verificación para sellerId={}", sellerId);
              return new SellerNotFoundException("No hay verificación para este vendedor");
            });
  }

  // ==================== Private helpers ====================

  /**
   * El microservicio guarda la cola en memoria: si se reinicia, la tarea desaparece y el vendedor
   * quedaría bloqueado en PROCESSING para siempre. Reenviamos la verificación con los objetos que
   * ya están en MinIO para que el flujo se recupere solo.
   */
  private SellerVerification resubmitLostTask(SellerVerification verification, UUID sellerId) {
    String selfieObject =
        verification.getSelfieMinioKey() != null
            ? verification.getSelfieMinioKey().getValue()
            : null;
    String documentObject =
        verification.getDocumentMinioKey() != null
            ? verification.getDocumentMinioKey().getValue()
            : null;

    if (selfieObject == null || documentObject == null) {
      log.error("Tarea perdida y sin imágenes para sellerId={}", sellerId);
      throw new com.eliteshop.colombia.seller.domain.exception.SellerVerificationException(
          "La verificación anterior se perdió. Sube la cédula y la selfie nuevamente.");
    }

    log.warn(
        "Tarea {} no existe en el microservicio; reenviando verificación para sellerId={}",
        verification.getTaskId() != null ? verification.getTaskId().getValue() : "N/A",
        sellerId);

    String newTaskId =
        faceMatcherAdapter.submitVerification(
            selfieObject, documentObject, verification.getFramesMinioKeys(), sellerId.toString());
    SellerVerification saved =
        repository.save(verification.withProcessing(new SellerVerificationTaskId(newTaskId)));

    log.info("Verificación reenviada para sellerId={}, nuevo taskId={}", sellerId, newTaskId);
    return saved;
  }

  private SellerVerification handleApproved(
      SellerVerification verification, FaceMatcherAdapter.VerificationResult result) {
    UUID sellerId = verification.getSellerId().getValue();

    verification =
        verification.approved(
            new SellerVerificationConfidenceScore(
                result.confidence() != null ? result.confidence() : 0.0));

    // Store detailed metrics
    verification =
        verification.withDetailedResult(
            new SellerVerificationConfidenceScore(
                result.confidence() != null ? result.confidence() : 0.0),
            result.livenessConfidence() != null
                ? new SellerVerificationLivenessConfidence(result.livenessConfidence())
                : null,
            result.antispoofScore() != null
                ? new SellerVerificationAntispoofScore(result.antispoofScore())
                : null,
            result.ocrCedulaNumber() != null
                ? new SellerVerificationOcrCedulaNumber(result.ocrCedulaNumber())
                : null,
            result.ocrCedulaName() != null
                ? new SellerVerificationOcrCedulaName(result.ocrCedulaName())
                : null);

    SellerVerification savedVerification = repository.save(verification);

    eventPublisher.publishEvent(
        new SellerVerificationCompletedEvent(
            sellerId,
            true,
            result.confidence() != null ? result.confidence() : 0.0,
            result.message() != null ? result.message() : "Identidad verificada",
            Instant.now()));

    log.info(
        "Verificación aprobada para sellerId={}, taskId={}, liveness={}, antispoof={}",
        sellerId,
        verification.getTaskId() != null ? verification.getTaskId().getValue() : "N/A",
        result.livenessConfidence(),
        result.antispoofScore());

    return savedVerification;
  }

  private SellerVerification handleRejected(
      SellerVerification verification, FaceMatcherAdapter.VerificationResult result) {
    UUID sellerId = verification.getSellerId().getValue();

    // El microservicio devuelve un mensaje genérico en «message» y el motivo real en
    // «result.reason». Guardamos el detallado para que el vendedor sepa qué corregir.
    String reason =
        result.reason() != null && !result.reason().isBlank()
            ? result.reason()
            : (result.message() != null && !result.message().isBlank()
                ? result.message()
                : "Verificación rechazada");

    verification =
        verification.rejected(
            new SellerVerificationConfidenceScore(
                result.confidence() != null ? result.confidence() : 0.0),
            new SellerVerificationRejectionReason(reason));

    // Store detailed metrics even on rejection
    verification =
        verification.withDetailedResult(
            new SellerVerificationConfidenceScore(
                result.confidence() != null ? result.confidence() : 0.0),
            result.livenessConfidence() != null
                ? new SellerVerificationLivenessConfidence(result.livenessConfidence())
                : null,
            result.antispoofScore() != null
                ? new SellerVerificationAntispoofScore(result.antispoofScore())
                : null,
            result.ocrCedulaNumber() != null
                ? new SellerVerificationOcrCedulaNumber(result.ocrCedulaNumber())
                : null,
            result.ocrCedulaName() != null
                ? new SellerVerificationOcrCedulaName(result.ocrCedulaName())
                : null);

    SellerVerification savedVerification = repository.save(verification);

    eventPublisher.publishEvent(
        new SellerVerificationCompletedEvent(
            sellerId,
            false,
            result.confidence() != null ? result.confidence() : 0.0,
            reason,
            Instant.now()));

    log.info(
        "Verificación rechazada para sellerId={}, taskId={}, reason={}",
        sellerId,
        verification.getTaskId() != null ? verification.getTaskId().getValue() : "N/A",
        result.message());

    return savedVerification;
  }

  private SellerVerification handlePending(SellerVerification verification, String remoteStatus) {
    log.info(
        "Verificación sellerId={} aún en estado '{}' en el microservicio",
        verification.getSellerId().getValue(),
        remoteStatus);
    return verification;
  }
}
