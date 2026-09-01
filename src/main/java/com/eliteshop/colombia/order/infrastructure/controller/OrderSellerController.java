package com.eliteshop.colombia.order.infrastructure.controller;

import com.eliteshop.colombia.order.application.*;
import com.eliteshop.colombia.order.domain.model.OrderStatus;
import com.eliteshop.colombia.order.infrastructure.controller.dto.OrderResponse;
import com.eliteshop.colombia.order.infrastructure.controller.dto.OrderStatusCountResponse;
import com.eliteshop.colombia.order.infrastructure.controller.dto.OrderSummaryResponse;
import com.eliteshop.colombia.order.infrastructure.controller.dto.SearchOrdersResponse;
import com.eliteshop.colombia.order.infrastructure.mapper.OrderMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderSellerController {

  private final FindOrdersBySellerUseCase findBySellerUseCase;
  private final OrderSummaryUseCase orderSummaryUseCase;
  private final SearchOrdersUseCase searchOrdersUseCase;
  private final OrderStatusCountsUseCase orderStatusCountsUseCase;
  private final OrderMapper mapper;
  private final OrderAuthorizationHelper authHelper;

  @GetMapping("/seller/{sellerId}")
  public ResponseEntity<List<OrderResponse>> findBySeller(
      @PathVariable UUID sellerId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "25") int size,
      Authentication authentication,
      HttpServletRequest request) {
    authHelper.requireSeller(sellerId, authentication, request);
    var result = findBySellerUseCase.execute(sellerId, page, size);
    var responses = result.content().stream().map(mapper::toResponse).collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/seller/{sellerId}/summary")
  public ResponseEntity<OrderSummaryResponse> getSellerSummary(
      @PathVariable UUID sellerId, Authentication authentication, HttpServletRequest request) {
    authHelper.requireSeller(sellerId, authentication, request);
    OrderSummaryResponse summary = orderSummaryUseCase.execute(sellerId);
    return ResponseEntity.ok(summary);
  }

  @GetMapping("/seller/{sellerId}/search")
  public ResponseEntity<SearchOrdersResponse> searchOrders(
      @PathVariable UUID sellerId,
      @RequestParam(required = false) OrderStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      Authentication authentication,
      HttpServletRequest request) {
    authHelper.requireSeller(sellerId, authentication, request);
    SearchOrdersUseCase.SearchResult result =
        searchOrdersUseCase.execute(sellerId, status, page, size);
    List<OrderResponse> orderResponses =
        result.orders().stream().map(mapper::toResponse).collect(Collectors.toList());
    return ResponseEntity.ok(
        new SearchOrdersResponse(
            orderResponses, result.total(), result.page(), result.size(), result.totalPages()));
  }

  @GetMapping("/seller/{sellerId}/status-counts")
  public ResponseEntity<OrderStatusCountResponse> getStatusCounts(
      @PathVariable UUID sellerId, Authentication authentication, HttpServletRequest request) {
    authHelper.requireSeller(sellerId, authentication, request);
    OrderStatusCountResponse counts = orderStatusCountsUseCase.execute(sellerId);
    return ResponseEntity.ok(counts);
  }
}
