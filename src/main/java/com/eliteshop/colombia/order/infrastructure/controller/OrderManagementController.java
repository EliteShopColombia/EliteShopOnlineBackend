package com.eliteshop.colombia.order.infrastructure.controller;

import com.eliteshop.colombia.order.application.*;
import com.eliteshop.colombia.order.domain.model.*;
import com.eliteshop.colombia.order.infrastructure.controller.dto.OrderRequest;
import com.eliteshop.colombia.order.infrastructure.controller.dto.OrderResponse;
import com.eliteshop.colombia.order.infrastructure.mapper.OrderMapper;
import com.eliteshop.colombia.shared.infrastructure.dto.PageResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderManagementController {

  private final OrderSaveUseCase saveUseCase;
  private final OrderUpdateUseCase updateUseCase;
  private final OrderDeleteUseCase deleteUseCase;
  private final OrderFindByIdUseCase findByIdUseCase;
  private final FindOrdersByCustomerIdUseCase findByCustomerIdUseCase;
  private final OrderMapper mapper;
  private final OrderAuthorizationHelper authHelper;

  @PostMapping
  public ResponseEntity<OrderResponse> save(
      @Valid @RequestBody OrderRequest request, Authentication authentication) {
    Order order =
        mapper.toDomainFromRequest(
            request, authHelper.authenticatedUserId(authentication, request.getCustomerId()));
    saveUseCase.execute(order);
    return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(order));
  }

  @PutMapping("/{id}")
  public ResponseEntity<OrderResponse> update(
      @PathVariable UUID id,
      @Valid @RequestBody OrderRequest request,
      Authentication authentication) {
    authHelper.requireCustomerOrder(id, authentication);
    Order order =
        mapper.toDomainFromRequest(
            request, authHelper.authenticatedUserId(authentication, request.getCustomerId()));
    order =
        new Order(
            new OrderId(id),
            new OrderCustomerId(request.getCustomerId()),
            OrderStatus.valueOf(
                request.getStatus() != null ? request.getStatus() : "PENDING_PAYMENT"),
            new OrderTotalAmount(request.getTotalAmount()),
            new OrderShippingAddress(request.getShippingAddress()),
            new OrderShippingDepartment(request.getShippingDepartment()),
            new OrderShippingCity(request.getShippingCity()),
            order.getCreatedAt(),
            order.getUpdatedAt(),
            order.getTrackingNumber(),
            order.getShippingCarrier(),
            order.getShippingLabelUrl(),
            order.getDisputeReason());
    Order updatedOrder = updateUseCase.execute(order);
    return ResponseEntity.ok(mapper.toResponse(updatedOrder));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication authentication) {
    authHelper.requireCustomerOrder(id, authentication);
    deleteUseCase.execute(new OrderId(id));
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  public ResponseEntity<PageResponse<OrderResponse>> findAll(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "25") int size,
      Authentication authentication) {
    UUID customerId = authHelper.authenticatedUserId(authentication, null);
    if (customerId == null) {
      return ResponseEntity.ok(new PageResponse<>(List.of(), 0, size, 0, 0));
    }
    var result = findByCustomerIdUseCase.execute(new OrderCustomerId(customerId), page, size);
    var responses = result.content().stream().map(mapper::toResponse).collect(Collectors.toList());
    return ResponseEntity.ok(
        PageResponse.of(responses, result.page(), result.size(), result.totalElements()));
  }

  @GetMapping("/{id}")
  public ResponseEntity<OrderResponse> findById(
      @PathVariable UUID id, Authentication authentication, HttpServletRequest request) {
    authHelper.requireOrderAccess(id, authentication, request);
    return findByIdUseCase
        .execute(new OrderId(id))
        .map(order -> ResponseEntity.ok(mapper.toResponseWithItems(order)))
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/customer/{customerId}")
  public ResponseEntity<PageResponse<OrderResponse>> findByCustomerId(
      @PathVariable UUID customerId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "25") int size,
      Authentication authentication) {
    UUID authenticatedCustomerId = authHelper.authenticatedUserId(authentication, customerId);
    if (authenticatedCustomerId == null || !authenticatedCustomerId.equals(customerId)) {
      throw new com.eliteshop.colombia.order.domain.exception.OrderAccessDeniedException(
          "No tienes permisos sobre las ordenes del cliente");
    }
    var result =
        findByCustomerIdUseCase.execute(new OrderCustomerId(authenticatedCustomerId), page, size);
    var responses = result.content().stream().map(mapper::toResponse).collect(Collectors.toList());
    return ResponseEntity.ok(
        PageResponse.of(responses, result.page(), result.size(), result.totalElements()));
  }
}
