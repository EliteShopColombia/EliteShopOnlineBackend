package com.eliteshop.colombia.seller.infrastructure.adapter;

import com.eliteshop.colombia.seller.infrastructure.config.FaceMatcherProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Adaptador de infraestructura para comunicarse con el microservicio de verificación facial
 * (FaceMatcher). Expone los endpoints síncronos legacy y los nuevos endpoints asincrónicos de
 * encolamiento.
 *
 * <p>Seguridad: Las URLs se configuran via {@link FaceMatcherProperties} (propiedades externas /
 * Config Server). No se exponen secrets en el código.
 */
@Slf4j
@Component
public class FaceMatcherAdapter {

  private final WebClient webClient;
  private final FaceMatcherProperties properties;

  public FaceMatcherAdapter(
      WebClient sellerVerificationWebClient, FaceMatcherProperties properties) {
    this.webClient = sellerVerificationWebClient;
    this.properties = properties;
  }

  // ==================== Legacy synchronous endpoint ====================

  public FaceMatchResult match(String selfieObject, String documentObject) {
    Map<String, String> request =
        Map.of("selfie_object", selfieObject, "cedula_object", documentObject);

    return webClient
        .post()
        .uri(properties.getUrl() + "/match")
        .headers(this::applyAuthHeader)
        .bodyValue(request)
        .retrieve()
        .bodyToMono(FaceMatchResult.class)
        .block();
  }

  /** Añade el secreto compartido exigido por el microservicio face-matcher. */
  private void applyAuthHeader(org.springframework.http.HttpHeaders headers) {
    if (properties.getSecret() != null && !properties.getSecret().isBlank()) {
      headers.set("X-Gateway-Secret", properties.getSecret());
    }
  }

  // ==================== Async verification endpoints ====================

  /**
   * Envía una tarea de verificación al microservicio. El microservicio encola la tarea y retorna un
   * {@code task_id} para consultas posteriores.
   *
   * @param selfieObject MinIO key de la selfie
   * @param documentObject MinIO key del documento (cédula)
   * @param sellerId ID del vendedor
   * @return taskId asignado por el microservicio
   * @throws VerificationSubmitException si la comunicación con el microservicio falla
   */
  public String submitVerification(String selfieObject, String documentObject, String sellerId) {
    VerificationSubmitRequest request =
        new VerificationSubmitRequest(selfieObject, documentObject, sellerId);

    log.info("Enviando tarea de verificación al microservicio para sellerId={}", sellerId);

    try {
      VerificationSubmitResponse response =
          webClient
              .post()
              .uri(properties.getUrl() + "/verify-seller")
              .headers(this::applyAuthHeader)
              .bodyValue(request)
              .retrieve()
              .bodyToMono(VerificationSubmitResponse.class)
              .block();

      if (response == null || response.taskId() == null) {
        throw new VerificationSubmitException(
            "El microservicio no devolvió un task_id válido para sellerId=" + sellerId);
      }

      log.info(
          "Tarea de verificación encolada exitosamente: taskId={}, sellerId={}",
          response.taskId(),
          sellerId);
      return response.taskId();
    } catch (WebClientResponseException e) {
      log.error(
          "Error del microservicio al enviar verificación para sellerId={}: HTTP {} - {}",
          sellerId,
          e.getStatusCode().value(),
          e.getResponseBodyAsString());
      throw new VerificationSubmitException(
          "Error del microservicio al enviar verificación: " + e.getMessage(), e);
    } catch (VerificationSubmitException e) {
      throw e;
    } catch (Exception e) {
      log.error(
          "Error de comunicación al enviar verificación para sellerId={}: {}",
          sellerId,
          e.getMessage());
      throw new VerificationSubmitException(
          "Error de comunicación con el microservicio: " + e.getMessage(), e);
    }
  }

  /**
   * Consulta el resultado de una tarea de verificación previamente encolada.
   *
   * @param taskId ID de la tarea devuelto por {@link #submitVerification}
   * @return resultado detallado de la verificación
   * @throws VerificationSubmitException si la comunicación con el microservicio falla
   */
  public VerificationResult getVerificationResult(String taskId) {
    log.info("Consultando resultado de verificación taskId={}", taskId);

    try {
      VerificationResult result =
          webClient
              .get()
              .uri(properties.getUrl() + "/verification-result/{taskId}", taskId)
              .headers(this::applyAuthHeader)
              .retrieve()
              .bodyToMono(VerificationResult.class)
              .block();

      if (result == null) {
        throw new VerificationSubmitException(
            "El microservicio devolvió null para taskId=" + taskId);
      }

      log.info("Resultado de verificación taskId={}: status={}", taskId, result.status());
      return result;
    } catch (WebClientResponseException e) {
      log.error(
          "Error del microservicio al consultar taskId={}: HTTP {} - {}",
          taskId,
          e.getStatusCode().value(),
          e.getResponseBodyAsString());
      throw new VerificationSubmitException(
          "Error del microservicio al consultar resultado: " + e.getMessage(), e);
    } catch (VerificationSubmitException e) {
      throw e;
    } catch (Exception e) {
      log.error("Error de comunicación al consultar taskId={}: {}", taskId, e.getMessage());
      throw new VerificationSubmitException(
          "Error de comunicación con el microservicio: " + e.getMessage(), e);
    }
  }

  // ==================== DTOs ====================

  /** Request para el endpoint POST /verify-seller. */
  public record VerificationSubmitRequest(
      @JsonProperty("selfie_object") String selfieObject,
      @JsonProperty("cedula_object") String documentObject,
      @JsonProperty("seller_id") String sellerId) {}

  /** Response del endpoint POST /verify-seller. */
  public record VerificationSubmitResponse(@JsonProperty("task_id") String taskId) {}

  /**
   * Resultado del endpoint GET /verification-result/{taskId}. Incluye métodos de conveniencia que
   * extraen los campos del objeto anidado {@code result}.
   */
  public record VerificationResult(
      @JsonProperty("status") String status,
      @JsonProperty("message") String message,
      @JsonProperty("task_id") String taskId,
      @JsonProperty("seller_id") String sellerId,
      @JsonProperty("result") VerificationResultDetail result) {

    /** Confidence del face match (extraído del nested result). */
    public Double confidence() {
      return result != null ? result.faceMatchConfidence() : null;
    }

    /** Liveness confidence (extraído del nested result). */
    public Double livenessConfidence() {
      return result != null ? result.livenessConfidence() : null;
    }

    /** Anti-spoof score (extraído del nested result). */
    public Double antispoofScore() {
      return result != null ? result.antispoofScore() : null;
    }

    /** Número de cédula detectado por OCR (extraído del nested result). */
    public String ocrCedulaNumber() {
      return result != null ? result.ocrCedulaNumber() : null;
    }

    /** Nombre detectado por OCR (extraído del nested result). */
    public String ocrCedulaName() {
      return result != null ? result.ocrCedulaName() : null;
    }

    /** Indica si el face match fue aprobado. */
    public boolean match() {
      return result != null && Boolean.TRUE.equals(result.approved());
    }

    /**
     * Factory method para crear un VerificationResult con la estructura plana (compatibilidad con
     * tests y el campo anidado {@code result}).
     */
    public static VerificationResult of(
        String status,
        Double confidence,
        Double livenessConfidence,
        Double antispoofScore,
        String ocrCedulaNumber,
        String ocrCedulaName,
        String message) {
      VerificationResultDetail detail =
          new VerificationResultDetail(
              confidence,
              livenessConfidence,
              antispoofScore,
              null,
              ocrCedulaNumber,
              ocrCedulaName,
              null,
              "approved".equalsIgnoreCase(status),
              message);
      return new VerificationResult(status, message, null, null, detail);
    }
  }

  /** Detalle anidado del resultado de verificación. */
  public record VerificationResultDetail(
      @JsonProperty("face_match_confidence") Double faceMatchConfidence,
      @JsonProperty("liveness_confidence") Double livenessConfidence,
      @JsonProperty("antispoof_score") Double antispoofScore,
      @JsonProperty("ocr_success") Boolean ocrSuccess,
      @JsonProperty("cedula_number") String ocrCedulaNumber,
      @JsonProperty("name") String ocrCedulaName,
      @JsonProperty("ocr_confidence") Double ocrConfidence,
      @JsonProperty("approved") Boolean approved,
      @JsonProperty("reason") String reason) {}

  /** Resultado legacy del endpoint síncrono POST /match. */
  public record FaceMatchResult(
      boolean match,
      double confidence,
      @JsonProperty("selfie_face_found") boolean selfieFaceFound,
      @JsonProperty("document_face_found") boolean documentFaceFound,
      String message) {}

  /** Excepción de dominio de infraestructura para fallos de comunicación con el microservicio. */
  public static class VerificationSubmitException extends RuntimeException {
    public VerificationSubmitException(String message) {
      super(message);
    }

    public VerificationSubmitException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
