package com.eliteshop.colombia.seller.infrastructure.controller.verification;

import com.eliteshop.colombia.seller.application.usecase.VerifySellerUseCase;
import com.eliteshop.colombia.seller.domain.model.verification.SellerVerification;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/sellers/{sellerId}/verification")
@RequiredArgsConstructor
public class SellerVerificationController {

  private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png");
  private static final long MAX_SIZE = 5 * 1024 * 1024;

  private final VerifySellerUseCase verifySellerUseCase;
  private final SellerVerificationMapperResponse mapper;

  @PostMapping(value = "/document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<?> uploadDocument(
      @PathVariable UUID sellerId, @RequestParam("file") MultipartFile file) {
    String validationError = validateFile(file);
    if (validationError != null) {
      return ResponseEntity.badRequest().body(validationError);
    }
    try {
      InputStream stream = file.getInputStream();
      SellerVerification verification =
          verifySellerUseCase.uploadDocument(sellerId, file.getOriginalFilename(), stream);
      return ResponseEntity.ok(mapper.toResponse(verification));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("Error subiendo el archivo");
    }
  }

  @PostMapping(value = "/selfie", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<?> uploadSelfie(
      @PathVariable UUID sellerId, @RequestParam("file") MultipartFile file) {
    String validationError = validateFile(file);
    if (validationError != null) {
      return ResponseEntity.badRequest().body(validationError);
    }
    try {
      InputStream stream = file.getInputStream();
      SellerVerification verification =
          verifySellerUseCase.uploadSelfie(sellerId, file.getOriginalFilename(), stream);
      return ResponseEntity.ok(mapper.toResponse(verification));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("Error subiendo el archivo");
    }
  }

  @PostMapping("/validate")
  public ResponseEntity<SellerVerificationResponse> validate(@PathVariable UUID sellerId) {
    SellerVerification verification = verifySellerUseCase.validate(sellerId);
    return ResponseEntity.ok(mapper.toResponse(verification));
  }

  @GetMapping
  public ResponseEntity<SellerVerificationResponse> getStatus(@PathVariable UUID sellerId) {
    SellerVerification verification = verifySellerUseCase.getStatus(sellerId);
    return ResponseEntity.ok(mapper.toResponse(verification));
  }

  private String validateFile(MultipartFile file) {
    if (file.isEmpty()) {
      return "El archivo esta vacio";
    }
    if (file.getSize() > MAX_SIZE) {
      return "La imagen excede 5MB";
    }
    String contentType = file.getContentType();
    if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
      return "Formato no soportado. Solo se permiten JPG y PNG";
    }
    return null;
  }
}
