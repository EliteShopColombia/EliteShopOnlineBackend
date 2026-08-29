package com.eliteshop.colombia.customer.infrastructure.controller;

import com.eliteshop.colombia.customer.application.CustomerAvatarUseCase;
import com.eliteshop.colombia.customer.application.CustomerDeleteUseCase;
import com.eliteshop.colombia.customer.application.CustomerFindAllUseCase;
import com.eliteshop.colombia.customer.application.CustomerFindByIdUseCase;
import com.eliteshop.colombia.customer.application.CustomerUpdateUseCase;
import com.eliteshop.colombia.customer.domain.model.*;
import com.eliteshop.colombia.customer.infrastructure.controller.dto.CustomerResponse;
import com.eliteshop.colombia.customer.infrastructure.controller.dto.UpdateCustomerRequest;
import com.eliteshop.colombia.customer.infrastructure.mapper.CustomerMapper;
import jakarta.validation.Valid;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

  private final CustomerUpdateUseCase updateUseCase;
  private final CustomerDeleteUseCase deleteUseCase;
  private final CustomerFindAllUseCase findAllUseCase;
  private final CustomerFindByIdUseCase findByIdUseCase;
  private final CustomerAvatarUseCase avatarUseCase;
  private final CustomerMapper mapper;
  private final com.eliteshop.colombia.shared.security.AuthorizationService authorizationService;

  private static final Set<String> ALLOWED_AVATAR_TYPES = Set.of("image/jpeg", "image/png");
  private static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024;

  @PutMapping("/{id}")
  public ResponseEntity<CustomerResponse> update(
      @PathVariable UUID id,
      @Valid @RequestBody UpdateCustomerRequest request,
      Authentication authentication) {
    authorizationService.requireCustomer(authentication, id);
    Customer existing = findByIdUseCase.execute(new CustomerId(id)).orElseThrow();

    CustomerInfo info = existing.getInfo();
    if (existing.getInfo() != null
        && (request.getAddress() != null
            || request.getDepartment() != null
            || request.getCity() != null
            || request.getDniType() != null
            || request.getDniNumber() != null)) {
      info =
          new CustomerInfo(
              request.getDniType() != null
                  ? new CustomerDniType(request.getDniType())
                  : existing.getInfo().getDniType(),
              request.getDniNumber() != null
                  ? new CustomerDniNumber(request.getDniNumber())
                  : existing.getInfo().getDniNumber(),
              request.getAddress() != null
                  ? new CustomerAddress(request.getAddress())
                  : existing.getInfo().getAddress(),
              request.getDepartment() != null
                  ? new CustomerDepartment(request.getDepartment())
                  : existing.getInfo().getDepartment(),
              request.getCity() != null
                  ? new CustomerCity(request.getCity())
                  : existing.getInfo().getCity(),
              existing.getInfo().getDniCreatedAt(),
              existing.getInfo().getDniUpdatedAt());
    }

    Customer customer =
        new Customer(
            existing.getId(),
            request.getFirstName() != null
                ? new CustomerFirstName(request.getFirstName())
                : existing.getFirstName(),
            request.getLastName() != null
                ? new CustomerLastName(request.getLastName())
                : existing.getLastName(),
            existing.getEmail(),
            request.getPhoneNumber() != null
                ? new CustomerPhoneNumber(request.getPhoneNumber())
                : existing.getPhoneNumber(),
            existing.getPassword(),
            request.getProfileImage() != null
                ? new CustomerProfileImage(request.getProfileImage())
                : existing.getProfileImage(),
            existing.getCreatedAt(),
            existing.getUpdatedAt(),
            info);
    updateUseCase.execute(customer);
    Customer updatedCustomer = findByIdUseCase.execute(new CustomerId(id)).orElseThrow();
    return ResponseEntity.ok(mapper.toResponse(updatedCustomer));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication authentication) {
    authorizationService.requireCustomer(authentication, id);
    deleteUseCase.execute(new CustomerId(id));
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  public ResponseEntity<
          com.eliteshop.colombia.shared.infrastructure.dto.PageResponse<CustomerResponse>>
      findAll(
          @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
          @org.springframework.web.bind.annotation.RequestParam(defaultValue = "25") int size) {
    var result = findAllUseCase.execute(page, size);
    var responses = result.content().stream().map(mapper::toResponse).collect(Collectors.toList());
    return ResponseEntity.ok(
        com.eliteshop.colombia.shared.infrastructure.dto.PageResponse.of(
            responses, result.page(), result.size(), result.totalElements()));
  }

  @GetMapping("/{id}")
  public ResponseEntity<CustomerResponse> findById(
      @PathVariable UUID id, Authentication authentication) {
    authorizationService.requireCustomer(authentication, id);
    return findByIdUseCase
        .execute(new CustomerId(id))
        .map(customer -> ResponseEntity.ok(mapper.toResponse(customer)))
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping("/{id}/avatar")
  public ResponseEntity<CustomerResponse> uploadAvatar(
      @PathVariable UUID id,
      @RequestParam("file") MultipartFile file,
      Authentication authentication) {
    authorizationService.requireCustomer(authentication, id);
    String validationError = validateAvatarFile(file);
    if (validationError != null) {
      return ResponseEntity.badRequest().build();
    }
    try {
      String objectKey =
          avatarUseCase.upload(id, file.getOriginalFilename(), file.getInputStream());
      Customer updated = findByIdUseCase.execute(new CustomerId(id)).orElseThrow();
      return ResponseEntity.ok(mapper.toResponse(updated));
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }

  @PutMapping("/{id}/avatar")
  public ResponseEntity<CustomerResponse> replaceAvatar(
      @PathVariable UUID id,
      @RequestParam("file") MultipartFile file,
      Authentication authentication) {
    return uploadAvatar(id, file, authentication);
  }

  @DeleteMapping("/{id}/avatar")
  public ResponseEntity<Void> deleteAvatar(@PathVariable UUID id, Authentication authentication) {
    authorizationService.requireCustomer(authentication, id);
    avatarUseCase.delete(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/avatar")
  public ResponseEntity<InputStreamResource> downloadAvatar(
      @PathVariable UUID id, Authentication authentication) {
    authorizationService.requireCustomer(authentication, id);
    try {
      InputStream stream = avatarUseCase.download(id);
      return ResponseEntity.ok()
          .contentType(MediaType.IMAGE_JPEG)
          .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
          .body(new InputStreamResource(stream));
    } catch (Exception e) {
      return ResponseEntity.notFound().build();
    }
  }

  private String validateAvatarFile(MultipartFile file) {
    if (file.isEmpty()) {
      return "El archivo esta vacio";
    }
    if (file.getSize() > MAX_AVATAR_SIZE) {
      return "La imagen excede 5MB";
    }
    String contentType = file.getContentType();
    if (contentType == null || !ALLOWED_AVATAR_TYPES.contains(contentType)) {
      return "Formato no soportado. Solo se permiten JPG y PNG";
    }
    return null;
  }
}
