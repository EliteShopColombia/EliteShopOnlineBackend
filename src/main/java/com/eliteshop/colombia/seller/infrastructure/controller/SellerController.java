package com.eliteshop.colombia.seller.infrastructure.controller;

import com.eliteshop.colombia.auth.infrastructure.config.JwtService;
import com.eliteshop.colombia.auth.infrastructure.controller.dto.AuthResponse;
import com.eliteshop.colombia.customer.domain.repository.CustomerRepository;
import com.eliteshop.colombia.seller.application.usecase.*;
import com.eliteshop.colombia.seller.domain.model.Seller;
import com.eliteshop.colombia.seller.domain.model.SellerId;
import com.eliteshop.colombia.seller.infrastructure.controller.dto.SellerBankInfoResponse;
import com.eliteshop.colombia.seller.infrastructure.controller.dto.SellerContactResponse;
import com.eliteshop.colombia.seller.infrastructure.controller.dto.SellerRequest;
import com.eliteshop.colombia.seller.infrastructure.controller.dto.SellerResponse;
import com.eliteshop.colombia.seller.infrastructure.mapper.SellerMapper;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SellerController {

  private final SellerSaveUseCase saveUseCase;
  private final SellerUpdateUseCase updateUseCase;
  private final SellerDeleteUseCase deleteUseCase;
  private final SellerFindAllUseCase findAllUseCase;
  private final SellerFindByIdUseCase findByIdUseCase;
  private final SellerFindContactBySellerIdUseCase findContactBySellerIdUseCase;
  private final SellerFindBankInfoBySellerIdUseCase findBankInfoBySellerIdUseCase;
  private final SellerMapper mapper;
  private final JwtService jwtService;
  private final CustomerRepository customerRepository;

  @PostMapping("/sellers")
  public ResponseEntity<AuthResponse> save(@Valid @RequestBody SellerRequest request) {
    Seller seller = mapper.toDomainFromRequest(request);
    saveUseCase.execute(seller);

    String email = seller.getContact().getEmail().getValue();
    String customerId =
        customerRepository
            .findByEmail(email)
            .map(c -> c.getId().getValue().toString())
            .orElse(seller.getId().getValue().toString());

    String token =
        jwtService.generateToken(customerId, email, "seller", seller.getId().getValue().toString());

    AuthResponse.UserInfo userInfo =
        new AuthResponse.UserInfo(
            UUID.fromString(customerId),
            email,
            seller.getFullname().getValue(),
            seller.getFullname().getValue(),
            "seller");
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new AuthResponse(token, jwtService.getExpiration(), userInfo));
  }

  @PutMapping("/sellers/{id}")
  public ResponseEntity<SellerResponse> update(
      @PathVariable UUID id, @Valid @RequestBody SellerRequest request) {
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
            seller.getContact(),
            seller.getBankInfo());
    updateUseCase.execute(seller);
    Seller updatedSeller = findByIdUseCase.execute(new SellerId(id)).orElseThrow();
    return ResponseEntity.ok(mapper.toResponse(updatedSeller));
  }

  @DeleteMapping("/sellers/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    deleteUseCase.execute(new SellerId(id));
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/sellers")
  public ResponseEntity<List<SellerResponse>> findAll() {
    List<Seller> sellers = findAllUseCase.execute();
    List<SellerResponse> responses =
        sellers.stream().map(mapper::toResponse).collect(Collectors.toList());
    return ResponseEntity.ok(responses);
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
      @PathVariable UUID sellerId) {
    return findBankInfoBySellerIdUseCase
        .execute(new SellerId(sellerId))
        .map(seller -> ResponseEntity.ok(mapper.toBankInfoResponse(seller)))
        .orElse(ResponseEntity.notFound().build());
  }
}
