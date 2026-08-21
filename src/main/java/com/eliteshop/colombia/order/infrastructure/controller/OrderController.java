package com.eliteshop.colombia.order.infrastructure.controller;

import com.eliteshop.colombia.order.application.AddTrackingEventUseCase;
import com.eliteshop.colombia.order.application.CancelOrderUseCase;
import com.eliteshop.colombia.order.application.CompleteOrderUseCase;
import com.eliteshop.colombia.order.application.ConfirmDeliveryUseCase;
import com.eliteshop.colombia.order.application.DisputeOrderUseCase;
import com.eliteshop.colombia.order.application.FindOrdersByCustomerIdUseCase;
import com.eliteshop.colombia.order.application.FindOrdersBySellerUseCase;
import com.eliteshop.colombia.order.application.GetTrackingEventsUseCase;
import com.eliteshop.colombia.order.application.OrderDeleteUseCase;
import com.eliteshop.colombia.order.application.OrderFindAllUseCase;
import com.eliteshop.colombia.order.application.OrderFindByIdUseCase;
import com.eliteshop.colombia.order.application.OrderSaveUseCase;
import com.eliteshop.colombia.order.application.OrderStatusCountsUseCase;
import com.eliteshop.colombia.order.application.OrderSummaryUseCase;
import com.eliteshop.colombia.order.application.OrderUpdateUseCase;
import com.eliteshop.colombia.order.application.OutForDeliveryUseCase;
import com.eliteshop.colombia.order.application.PrepareOrderUseCase;
import com.eliteshop.colombia.order.application.RefundOrderUseCase;
import com.eliteshop.colombia.order.application.SearchOrdersUseCase;
import com.eliteshop.colombia.order.application.ShipOrderUseCase;
import com.eliteshop.colombia.order.application.UpdateTrackingUseCase;
import com.eliteshop.colombia.order.domain.model.Order;
import com.eliteshop.colombia.order.domain.model.OrderCustomerId;
import com.eliteshop.colombia.order.domain.model.OrderId;
import com.eliteshop.colombia.order.domain.model.OrderShippingAddress;
import com.eliteshop.colombia.order.domain.model.OrderShippingCity;
import com.eliteshop.colombia.order.domain.model.OrderShippingDepartment;
import com.eliteshop.colombia.order.domain.model.OrderStatus;
import com.eliteshop.colombia.order.domain.model.OrderTotalAmount;
import com.eliteshop.colombia.order.domain.model.tracking.TrackingEvent;
import com.eliteshop.colombia.order.infrastructure.controller.dto.OrderRequest;
import com.eliteshop.colombia.order.infrastructure.controller.dto.OrderResponse;
import com.eliteshop.colombia.order.infrastructure.controller.dto.OrderStatusCountResponse;
import com.eliteshop.colombia.order.infrastructure.controller.dto.OrderSummaryResponse;
import com.eliteshop.colombia.order.infrastructure.controller.dto.SearchOrdersResponse;
import com.eliteshop.colombia.order.infrastructure.controller.dto.TrackingEventRequest;
import com.eliteshop.colombia.order.infrastructure.controller.dto.TrackingEventResponse;
import com.eliteshop.colombia.order.infrastructure.controller.dto.TrackingInfoRequest;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
  private final FindOrdersByCustomerIdUseCase findByCustomerIdUseCase;
  private final FindOrdersBySellerUseCase findBySellerUseCase;
  private final CancelOrderUseCase cancelOrderUseCase;
  private final ConfirmDeliveryUseCase confirmDeliveryUseCase;
  private final PrepareOrderUseCase prepareOrderUseCase;
  private final ShipOrderUseCase shipOrderUseCase;
  private final OutForDeliveryUseCase outForDeliveryUseCase;
  private final UpdateTrackingUseCase updateTrackingUseCase;
  private final CompleteOrderUseCase completeOrderUseCase;
  private final DisputeOrderUseCase disputeOrderUseCase;
  private final RefundOrderUseCase refundOrderUseCase;
  private final OrderSummaryUseCase orderSummaryUseCase;
  private final AddTrackingEventUseCase addTrackingEventUseCase;
  private final GetTrackingEventsUseCase getTrackingEventsUseCase;
  private final SearchOrdersUseCase searchOrdersUseCase;
  private final OrderStatusCountsUseCase orderStatusCountsUseCase;
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
            order.getShippingLabelUrl());
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
        .map(order -> ResponseEntity.ok(mapper.toResponseWithItems(order)))
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/customer/{customerId}")
  public ResponseEntity<List<OrderResponse>> findByCustomerId(@PathVariable UUID customerId) {
    List<Order> orders = findByCustomerIdUseCase.execute(new OrderCustomerId(customerId));
    List<OrderResponse> responses =
        orders.stream().map(mapper::toResponse).collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/seller/{sellerId}")
  public ResponseEntity<List<OrderResponse>> findBySeller(@PathVariable UUID sellerId) {
    List<Order> orders = findBySellerUseCase.execute(sellerId);
    List<OrderResponse> responses =
        orders.stream().map(mapper::toResponse).collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/seller/{sellerId}/summary")
  public ResponseEntity<OrderSummaryResponse> getSellerSummary(@PathVariable UUID sellerId) {
    OrderSummaryResponse summary = orderSummaryUseCase.execute(sellerId);
    return ResponseEntity.ok(summary);
  }

  @GetMapping("/seller/{sellerId}/search")
  public ResponseEntity<SearchOrdersResponse> searchOrders(
      @PathVariable UUID sellerId,
      @RequestParam(required = false) OrderStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    SearchOrdersUseCase.SearchResult result =
        searchOrdersUseCase.execute(sellerId, status, page, size);
    List<OrderResponse> orderResponses =
        result.orders().stream().map(mapper::toResponse).collect(Collectors.toList());
    return ResponseEntity.ok(
        new SearchOrdersResponse(
            orderResponses, result.total(), result.page(), result.size(), result.totalPages()));
  }

  @GetMapping("/seller/{sellerId}/status-counts")
  public ResponseEntity<OrderStatusCountResponse> getStatusCounts(@PathVariable UUID sellerId) {
    OrderStatusCountResponse counts = orderStatusCountsUseCase.execute(sellerId);
    return ResponseEntity.ok(counts);
  }

  @GetMapping("/{id}/tracking")
  public ResponseEntity<List<TrackingEventResponse>> getTrackingEvents(@PathVariable UUID id) {
    List<TrackingEvent> events = getTrackingEventsUseCase.execute(new OrderId(id));
    List<TrackingEventResponse> responses =
        events.stream().map(this::toTrackingEventResponse).collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  @PostMapping("/{id}/tracking")
  public ResponseEntity<TrackingEventResponse> addTrackingEvent(
      @PathVariable UUID id, @Valid @RequestBody TrackingEventRequest request) {
    TrackingEvent event =
        addTrackingEventUseCase.execute(
            new OrderId(id),
            request.getStatus(),
            request.getLocation(),
            request.getDescription(),
            request.getEventTimestamp());
    return ResponseEntity.status(HttpStatus.CREATED).body(toTrackingEventResponse(event));
  }

  @PatchMapping("/{id}/cancel")
  public ResponseEntity<Void> cancel(@PathVariable UUID id) {
    cancelOrderUseCase.execute(new OrderId(id));
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/confirm-delivery")
  public ResponseEntity<Void> confirmDelivery(@PathVariable UUID id) {
    confirmDeliveryUseCase.execute(new OrderId(id));
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/prepare")
  public ResponseEntity<Void> prepare(@PathVariable UUID id) {
    prepareOrderUseCase.execute(new OrderId(id));
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/ship")
  public ResponseEntity<Void> ship(@PathVariable UUID id) {
    shipOrderUseCase.execute(new OrderId(id));
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/out-for-delivery")
  public ResponseEntity<Void> outForDelivery(@PathVariable UUID id) {
    outForDeliveryUseCase.execute(new OrderId(id));
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/tracking")
  public ResponseEntity<Void> updateTracking(
      @PathVariable UUID id, @Valid @RequestBody TrackingInfoRequest request) {
    updateTrackingUseCase.execute(
        new OrderId(id),
        request.getTrackingNumber(),
        request.getShippingCarrier(),
        request.getShippingLabelUrl());
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/complete")
  public ResponseEntity<Void> complete(@PathVariable UUID id) {
    completeOrderUseCase.execute(new OrderId(id));
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/dispute")
  public ResponseEntity<Void> dispute(@PathVariable UUID id) {
    disputeOrderUseCase.execute(new OrderId(id));
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/refund")
  public ResponseEntity<Void> refund(@PathVariable UUID id) {
    refundOrderUseCase.execute(new OrderId(id));
    return ResponseEntity.noContent().build();
  }

  private TrackingEventResponse toTrackingEventResponse(TrackingEvent event) {
    return new TrackingEventResponse(
        event.getId().getValue(),
        event.getOrderId().getValue(),
        event.getStatus().name(),
        event.getLocation(),
        event.getDescription(),
        event.getEventTimestamp(),
        event.getCreatedAt());
  }
}
