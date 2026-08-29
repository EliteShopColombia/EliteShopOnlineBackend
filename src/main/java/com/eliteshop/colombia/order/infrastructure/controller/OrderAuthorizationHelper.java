package com.eliteshop.colombia.order.infrastructure.controller;

import com.eliteshop.colombia.order.application.OrderFindByIdUseCase;
import com.eliteshop.colombia.order.domain.exception.OrderAccessDeniedException;
import com.eliteshop.colombia.order.domain.exception.OrderNotFoundException;
import com.eliteshop.colombia.order.domain.model.Order;
import com.eliteshop.colombia.order.domain.model.OrderId;
import com.eliteshop.colombia.order.domain.repository.OrderItemRepository;
import com.eliteshop.colombia.shared.security.AuthorizationService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderAuthorizationHelper {

  private final OrderFindByIdUseCase findByIdUseCase;
  private final OrderItemRepository orderItemRepository;
  private final AuthorizationService authorizationService;

  public void requireCustomerOrder(UUID orderId, Authentication authentication) {
    if (authorizationService == null) {
      return;
    }
    Order order =
        findByIdUseCase
            .execute(new OrderId(orderId))
            .orElseThrow(() -> new OrderNotFoundException("Orden no encontrada"));
    authorizationService.requireCustomer(authentication, order.getCustomerId().getValue());
  }

  public void requireOrderAccess(
      UUID orderId, Authentication authentication, HttpServletRequest request) {
    if (authorizationService == null) {
      return;
    }
    Order order =
        findByIdUseCase
            .execute(new OrderId(orderId))
            .orElseThrow(() -> new OrderNotFoundException("Orden no encontrada"));
    UUID authenticatedUserId = authorizationService.authenticatedUserId(authentication);
    if (order.getCustomerId().getValue().equals(authenticatedUserId)) {
      return;
    }
    String sellerId = (String) request.getAttribute("gateway.sellerId");
    if (sellerId != null
        && orderItemRepository.findByOrderId(orderId).stream()
            .anyMatch(item -> sellerId.equals(item.getSellerId().getValue().toString()))) {
      return;
    }
    throw new OrderAccessDeniedException("No tienes permisos sobre esta orden");
  }

  public void requireSellerOrder(
      UUID orderId, Authentication authentication, HttpServletRequest request) {
    if (authorizationService == null) {
      return;
    }
    UUID sellerId =
        orderItemRepository.findByOrderId(orderId).stream()
            .findFirst()
            .map(item -> item.getSellerId().getValue())
            .orElseThrow(() -> new OrderAccessDeniedException("La orden no tiene vendedor"));
    requireSeller(sellerId, authentication, request);
    String gatewaySellerId = (String) request.getAttribute("gateway.sellerId");
    if (gatewaySellerId == null
        || orderItemRepository.findByOrderId(orderId).stream()
            .noneMatch(item -> gatewaySellerId.equals(item.getSellerId().getValue().toString()))) {
      throw new OrderAccessDeniedException("No tienes permisos sobre esta orden");
    }
  }

  public void requireSeller(
      UUID sellerId, Authentication authentication, HttpServletRequest request) {
    if (authorizationService == null) {
      return;
    }
    authorizationService.requireSeller(
        authentication, sellerId, (String) request.getAttribute("gateway.sellerId"));
  }

  public UUID authenticatedUserId(Authentication authentication, UUID fallbackUserId) {
    if (authorizationService == null) {
      return fallbackUserId;
    }
    return authorizationService.authenticatedUserId(authentication);
  }
}
