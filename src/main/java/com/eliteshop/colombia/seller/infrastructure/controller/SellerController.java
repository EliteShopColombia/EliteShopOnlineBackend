package com.eliteshop.colombia.seller.infrastructure.controller;

import com.eliteshop.colombia.auth.infrastructure.controller.dto.AuthResponse;
import com.eliteshop.colombia.seller.application.SellerAvatarUseCase;
import com.eliteshop.colombia.seller.application.usecase.*;
import com.eliteshop.colombia.seller.domain.model.Seller;
import com.eliteshop.colombia.seller.domain.model.SellerId;
import com.eliteshop.colombia.seller.infrastructure.controller.dto.SellerBankInfoResponse;
import com.eliteshop.colombia.seller.infrastructure.controller.dto.SellerContactResponse;
import com.eliteshop.colombia.seller.infrastructure.controller.dto.SellerRequest;
import com.eliteshop.colombia.seller.infrastructure.controller.dto.SellerResponse;
import com.eliteshop.colombia.seller.infrastructure.mapper.SellerMapper;
import jakarta.validation.Valid;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SellerController {

  private final SellerRegistrationUseCase registrationUseCase;
  private final SellerUpdateUseCase updateUseCase;
  private final SellerDeleteUseCase deleteUseCase;
  private final SellerFindAllUseCase findAllUseCase;
  private final SellerFindByIdUseCase findByIdUseCase;
  private final SellerFindContactBySellerIdUseCase findContactBySellerIdUseCase;
  private final SellerFindBankInfoBySellerIdUseCase findBankInfoBySellerIdUseCase;
  private final SellerAvatarUseCase avatarUseCase;
  private final SellerMapper mapper;
  private final AvatarFileValidator avatarFileValidator;
  private final com.eliteshop.colombia.shared.security.AuthorizationService authorizationService;

  @PostMapping("/sellers")
  public ResponseEntity<AuthResponse> save(@Valid @RequestBody SellerRequest request) {
    Seller seller = mapper.toDomainFromRequest(request);
    AuthResponse authResponse = registrationUseCase.execute(seller);
    return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
  }

  @PutMapping("/sellers/{id}")
  public ResponseEntity<SellerResponse> update(
      @PathVariable UUID id,
      @RequestBody SellerRequest request,
      Authentication authentication,
      jakarta.servlet.http.HttpServletRequest httpRequest) {
    authorizationService.requireSeller(
        authentication, id, (String) httpRequest.getAttribute("gateway.sellerId"));
    Seller seller = mapper.toDomainFromRequest(request);
    seller =
        new Seller(
            new SellerId(id),
            seller.getTypeTrade(),
            seller.getTypeDni(),
            seller.getDniNumber(),
            seller.getTradeName(),
            seller.getFullname(),
            seller.getIsActive(),
            seller.getIsVerified(),
            seller.getCreatedAt(),
            seller.getUpdatedAt(),
            seller.getProfileImage(),
            seller.getContact(),
            seller.getBankInfo());
    updateUseCase.execute(seller);
    Seller updatedSeller = findByIdUseCase.execute(new SellerId(id)).orElseThrow();
    return ResponseEntity.ok(mapper.toResponse(updatedSeller));
  }

  @DeleteMapping("/sellers/{id}")
  public ResponseEntity<Void> delete(
      @PathVariable UUID id,
      Authentication authentication,
      jakarta.servlet.http.HttpServletRequest httpRequest) {
    authorizationService.requireSeller(
        authentication, id, (String) httpRequest.getAttribute("gateway.sellerId"));
    deleteUseCase.execute(new SellerId(id));
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/sellers")
  public ResponseEntity<
          com.eliteshop.colombia.shared.infrastructure.dto.PageResponse<SellerResponse>>
      findAll(
          @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size) {
    var result = findAllUseCase.execute(page, size);
    var responses = result.content().stream().map(mapper::toResponse).collect(Collectors.toList());
    return ResponseEntity.ok(
        com.eliteshop.colombia.shared.infrastructure.dto.PageResponse.of(
            responses, result.page(), result.size(), result.totalElements()));
  }

  @GetMapping("/sellers/{id}")
  public ResponseEntity<SellerResponse> findById(@PathVariable UUID id) {
    return findByIdUseCase
        .execute(new SellerId(id))
        .map(seller -> ResponseEntity.ok(mapper.toResponse(seller)))
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/seller-contact/{sellerId}")
  public ResponseEntity<SellerContactResponse> findContactBySellerId(@PathVariable UUID sellerId) {
    return findContactBySellerIdUseCase
        .execute(new SellerId(sellerId))
        .map(seller -> ResponseEntity.ok(mapper.toContactResponse(seller)))
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/seller-info/{sellerId}")
  public ResponseEntity<SellerBankInfoResponse> findBankInfoBySellerId(
      @PathVariable UUID sellerId,
      Authentication authentication,
      jakarta.servlet.http.HttpServletRequest httpRequest) {
    authorizationService.requireSeller(
        authentication, sellerId, (String) httpRequest.getAttribute("gateway.sellerId"));
    return findBankInfoBySellerIdUseCase
        .execute(new SellerId(sellerId))
        .map(seller -> ResponseEntity.ok(mapper.toBankInfoResponse(seller)))
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping("/sellers/{id}/avatar")
  public ResponseEntity<SellerResponse> uploadAvatar(
      @PathVariable UUID id,
      @RequestParam("file") MultipartFile file,
      Authentication authentication,
      jakarta.servlet.http.HttpServletRequest httpRequest) {
    authorizationService.requireSeller(
        authentication, id, (String) httpRequest.getAttribute("gateway.sellerId"));
    avatarFileValidator.validate(file);
    try {
      avatarUseCase.upload(id, file.getOriginalFilename(), file.getInputStream());
      Seller updated = findByIdUseCase.execute(new SellerId(id)).orElseThrow();
      return ResponseEntity.ok(mapper.toResponse(updated));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.error("Error subiendo avatar de seller {}", id, e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @PutMapping("/sellers/{id}/avatar")
  public ResponseEntity<SellerResponse> replaceAvatar(
      @PathVariable UUID id,
      @RequestParam("file") MultipartFile file,
      Authentication authentication,
      jakarta.servlet.http.HttpServletRequest httpRequest) {
    return uploadAvatar(id, file, authentication, httpRequest);
  }

  @DeleteMapping("/sellers/{id}/avatar")
  public ResponseEntity<Void> deleteAvatar(
      @PathVariable UUID id,
      Authentication authentication,
      jakarta.servlet.http.HttpServletRequest httpRequest) {
    authorizationService.requireSeller(
        authentication, id, (String) httpRequest.getAttribute("gateway.sellerId"));
    avatarUseCase.delete(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/sellers/{id}/avatar")
  public ResponseEntity<InputStreamResource> downloadAvatar(
      @PathVariable UUID id, Authentication authentication) {
    try {
      java.io.InputStream stream = avatarUseCase.download(id);
      return ResponseEntity.ok()
          .contentType(MediaType.IMAGE_JPEG)
          .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
          .body(new InputStreamResource(stream));
    } catch (Exception e) {
      return ResponseEntity.notFound().build();
    }
  }
}
