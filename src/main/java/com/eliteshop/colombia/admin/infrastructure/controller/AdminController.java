package com.eliteshop.colombia.admin.infrastructure.controller;

import com.eliteshop.colombia.admin.infrastructure.controller.dto.AdminDashboardResponse;
import com.eliteshop.colombia.admin.infrastructure.controller.dto.SellerStatusRequest;
import com.eliteshop.colombia.customer.infrastructure.persistence.CustomerJpaRepository;
import com.eliteshop.colombia.order.infrastructure.persistence.OrderEntity;
import com.eliteshop.colombia.order.infrastructure.persistence.OrderJpaRepository;
import com.eliteshop.colombia.product.infrastructure.persistence.ProductJpaRepository;
import com.eliteshop.colombia.seller.infrastructure.persistence.SellerEntity;
import com.eliteshop.colombia.seller.infrastructure.persistence.SellerJpaRepository;
import com.eliteshop.colombia.shared.domain.PageResult;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

  private final CustomerJpaRepository customerJpaRepository;
  private final SellerJpaRepository sellerJpaRepository;
  private final OrderJpaRepository orderJpaRepository;
  private final ProductJpaRepository productJpaRepository;

  @GetMapping("/dashboard")
  public ResponseEntity<AdminDashboardResponse> getDashboard() {
    long totalCustomers = customerJpaRepository.count();
    long totalSellers = sellerJpaRepository.count();
    long activeSellers =
        sellerJpaRepository.findAll().stream().filter(SellerEntity::getIsActive).count();
    long totalOrders = orderJpaRepository.count();
    BigDecimal totalRevenue =
        orderJpaRepository.findAll().stream()
            .map(OrderEntity::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    long totalProducts = productJpaRepository.count();

    AdminDashboardResponse response =
        AdminDashboardResponse.builder()
            .totalCustomers(totalCustomers)
            .totalSellers(totalSellers)
            .activeSellers(activeSellers)
            .totalOrders(totalOrders)
            .totalRevenue(totalRevenue)
            .totalProducts(totalProducts)
            .build();
    return ResponseEntity.ok(response);
  }

  @GetMapping("/customers")
  public ResponseEntity<PageResult<?>> getAllCustomers(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size) {
    var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    var result = customerJpaRepository.findAll(pageable);
    return ResponseEntity.ok(
        PageResult.of(result.getContent(), page, size, result.getTotalElements()));
  }

  @GetMapping("/sellers")
  public ResponseEntity<PageResult<?>> getAllSellers(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size) {
    var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    var result = sellerJpaRepository.findAll(pageable);
    return ResponseEntity.ok(
        PageResult.of(result.getContent(), page, size, result.getTotalElements()));
  }

  @GetMapping("/sellers/{id}")
  public ResponseEntity<SellerEntity> getSellerById(@PathVariable UUID id) {
    return sellerJpaRepository
        .findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PatchMapping("/sellers/{id}/status")
  public ResponseEntity<Void> updateSellerStatus(
      @PathVariable UUID id, @Valid @RequestBody SellerStatusRequest request) {
    var seller = sellerJpaRepository.findById(id);
    if (seller.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    SellerEntity entity = seller.get();
    entity.setIsActive(request.getIsActive());
    entity.setUpdatedAt(Timestamp.from(Instant.now()));
    sellerJpaRepository.save(entity);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/orders")
  public ResponseEntity<?> getAllOrders(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size) {
    var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    var result = orderJpaRepository.findAll(pageable);
    return ResponseEntity.ok(
        PageResult.of(result.getContent(), page, size, result.getTotalElements()));
  }
}
