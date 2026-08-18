package com.eliteshop.colombia.order.infrastructure.controller;

import com.eliteshop.colombia.order.application.OrderDeleteUseCase;
import com.eliteshop.colombia.order.application.OrderFindAllUseCase;
import com.eliteshop.colombia.order.application.OrderFindByIdUseCase;
import com.eliteshop.colombia.order.application.OrderSaveUseCase;
import com.eliteshop.colombia.order.application.OrderUpdateUseCase;
import com.eliteshop.colombia.order.domain.model.Order;
import com.eliteshop.colombia.order.domain.model.OrderCustomerId;
import com.eliteshop.colombia.order.domain.model.OrderId;
import com.eliteshop.colombia.order.domain.model.OrderShippingAddress;
import com.eliteshop.colombia.order.domain.model.OrderShippingCity;
import com.eliteshop.colombia.order.domain.model.OrderShippingDepartment;
import com.eliteshop.colombia.order.domain.model.OrderStatus;
import com.eliteshop.colombia.order.domain.model.OrderTotalAmount;
import com.eliteshop.colombia.order.infrastructure.controller.dto.OrderRequest;
import com.eliteshop.colombia.order.infrastructure.controller.dto.OrderResponse;
import com.eliteshop.colombia.order.infrastructure.mapper.OrderMapper;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

  private final OrderSaveUseCase saveUseCase;
  private final OrderUpdateUseCase updateUseCase;
  private final OrderDeleteUseCase deleteUseCase;
  private final OrderFindAllUseCase findAllUseCase;
  private final OrderFindByIdUseCase findByIdUseCase;
  private final OrderMapper mapper;

  @PostMapping
  public ResponseEntity<OrderResponse> save(@Valid @RequestBody OrderRequest request) {
    Order order = mapper.toDomainFromRequest(request);
    saveUseCase.execute(order);
    return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(order));
  }

  @PutMapping("/{id}")
  public ResponseEntity<OrderResponse> update(
      @PathVariable UUID id, @Valid @RequestBody OrderRequest request) {
    Order order = mapper.toDomainFromRequest(request);
    order =
        new Order(
            new OrderId(id),
            new OrderCustomerId(request.getCustomerId()),
            OrderStatus.valueOf(request.getStatus() != null ? request.getStatus() : "PENDING"),
            new OrderTotalAmount(request.getTotalAmount()),
            new OrderShippingAddress(request.getShippingAddress()),
            new OrderShippingDepartment(request.getShippingDepartment()),
            new OrderShippingCity(request.getShippingCity()),
            order.getCreatedAt(),
            order.getUpdatedAt());
    updateUseCase.execute(order);
    Order updatedOrder = findByIdUseCase.execute(new OrderId(id)).orElseThrow();
    return ResponseEntity.ok(mapper.toResponse(updatedOrder));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    deleteUseCase.execute(new OrderId(id));
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  public ResponseEntity<List<OrderResponse>> findAll() {
    List<Order> orders = findAllUseCase.execute();
    List<OrderResponse> responses =
        orders.stream().map(mapper::toResponse).collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/{id}")
  public ResponseEntity<OrderResponse> findById(@PathVariable UUID id) {
    return findByIdUseCase
        .execute(new OrderId(id))
        .map(order -> ResponseEntity.ok(mapper.toResponse(order)))
        .orElse(ResponseEntity.notFound().build());
  }
}
