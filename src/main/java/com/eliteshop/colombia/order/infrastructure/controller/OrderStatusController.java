package com.eliteshop.colombia.order.infrastructure.controller;

import com.eliteshop.colombia.order.application.*;
import com.eliteshop.colombia.order.domain.model.OrderId;
import com.eliteshop.colombia.order.domain.model.tracking.TrackingEvent;
import com.eliteshop.colombia.order.infrastructure.controller.dto.*;
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
public class OrderStatusController {

  private final CancelOrderUseCase cancelOrderUseCase;
  private final ConfirmDeliveryUseCase confirmDeliveryUseCase;
  private final PrepareOrderUseCase prepareOrderUseCase;
  private final ShipOrderUseCase shipOrderUseCase;
  private final OutForDeliveryUseCase outForDeliveryUseCase;
  private final CompleteOrderUseCase completeOrderUseCase;
  private final DisputeOrderUseCase disputeOrderUseCase;
  private final RefundOrderUseCase refundOrderUseCase;
  private final UpdateTrackingUseCase updateTrackingUseCase;
  private final AddTrackingEventUseCase addTrackingEventUseCase;
  private final GetTrackingEventsUseCase getTrackingEventsUseCase;
  private final OrderAuthorizationHelper authHelper;

  @GetMapping("/{id}/tracking")
  public ResponseEntity<List<TrackingEventResponse>> getTrackingEvents(
      @PathVariable UUID id, Authentication authentication, HttpServletRequest request) {
    authHelper.requireOrderAccess(id, authentication, request);
    List<TrackingEvent> events = getTrackingEventsUseCase.execute(new OrderId(id));
    List<TrackingEventResponse> responses =
        events.stream().map(this::toTrackingEventResponse).collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  @PostMapping("/{id}/tracking")
  public ResponseEntity<TrackingEventResponse> addTrackingEvent(
      @PathVariable UUID id,
      @Valid @RequestBody TrackingEventRequest request,
      Authentication authentication,
      HttpServletRequest httpRequest) {
    authHelper.requireSellerOrder(id, authentication, httpRequest);
    TrackingEvent event =
        addTrackingEventUseCase.execute(
            new OrderId(id),
            request.getStatus(),
            request.getLocation(),
            request.getDescription(),
            request.getEventTimestamp());
    return ResponseEntity.status(HttpStatus.CREATED).body(toTrackingEventResponse(event));
  }

  @PatchMapping("/{id}/tracking")
  public ResponseEntity<Void> updateTracking(
      @PathVariable UUID id,
      @Valid @RequestBody TrackingInfoRequest request,
      Authentication authentication,
      HttpServletRequest httpRequest) {
    authHelper.requireSellerOrder(id, authentication, httpRequest);
    updateTrackingUseCase.execute(
        new OrderId(id),
        request.getTrackingNumber(),
        request.getShippingCarrier(),
        request.getShippingLabelUrl());
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/cancel")
  public ResponseEntity<Void> cancel(@PathVariable UUID id, Authentication authentication) {
    authHelper.requireCustomerOrder(id, authentication);
    cancelOrderUseCase.execute(new OrderId(id));
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/confirm-delivery")
  public ResponseEntity<Void> confirmDelivery(
      @PathVariable UUID id, Authentication authentication) {
    authHelper.requireCustomerOrder(id, authentication);
    confirmDeliveryUseCase.execute(new OrderId(id));
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/prepare")
  public ResponseEntity<Void> prepare(
      @PathVariable UUID id, Authentication authentication, HttpServletRequest request) {
    authHelper.requireSellerOrder(id, authentication, request);
    prepareOrderUseCase.execute(new OrderId(id));
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/ship")
  public ResponseEntity<Void> ship(
      @PathVariable UUID id, Authentication authentication, HttpServletRequest request) {
    authHelper.requireSellerOrder(id, authentication, request);
    shipOrderUseCase.execute(new OrderId(id));
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/out-for-delivery")
  public ResponseEntity<Void> outForDelivery(
      @PathVariable UUID id, Authentication authentication, HttpServletRequest request) {
    authHelper.requireSellerOrder(id, authentication, request);
    outForDeliveryUseCase.execute(new OrderId(id));
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/complete")
  public ResponseEntity<Void> complete(
      @PathVariable UUID id, Authentication authentication, HttpServletRequest request) {
    authHelper.requireSellerOrder(id, authentication, request);
    completeOrderUseCase.execute(new OrderId(id));
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/dispute")
  public ResponseEntity<Void> dispute(
      @PathVariable UUID id,
      @Valid @RequestBody DisputeRequest disputeRequest,
      Authentication authentication) {
    authHelper.requireCustomerOrder(id, authentication);
    disputeOrderUseCase.execute(
        new OrderId(id), UUID.fromString(authentication.getName()), disputeRequest.getReason());
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/refund")
  public ResponseEntity<Void> refund(@PathVariable UUID id, Authentication authentication) {
    authHelper.requireCustomerOrder(id, authentication);
    refundOrderUseCase.execute(new OrderId(id), UUID.fromString(authentication.getName()));
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
