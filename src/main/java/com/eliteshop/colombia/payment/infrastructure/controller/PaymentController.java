package com.eliteshop.colombia.payment.infrastructure.controller;

import com.eliteshop.colombia.customer.domain.model.CustomerId;
import com.eliteshop.colombia.customer.domain.repository.CustomerRepository;
import com.eliteshop.colombia.order.domain.model.OrderId;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import com.eliteshop.colombia.payment.application.usecase.*;
import com.eliteshop.colombia.payment.domain.model.*;
import com.eliteshop.colombia.payment.infrastructure.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

  private final CreateCheckoutSessionUseCase createCheckoutSessionUseCase;
  private final ConfirmPaymentUseCase confirmPaymentUseCase;
  private final RetryPaymentUseCase retryPaymentUseCase;
  private final RetryWithSavedCardUseCase retryWithSavedCardUseCase;
  private final OrderRepository orderRepository;
  private final CustomerRepository customerRepository;

  @PostMapping("/checkout-session")
  public ResponseEntity<CreateCheckoutSessionResponse> createCheckoutSession(
      HttpServletRequest request, @Valid @RequestBody CreateCheckoutSessionRequest req) {

    UUID customerId = UUID.fromString((String) request.getAttribute("gateway.userId"));

    log.info(
        "Creando sesión de checkout para orderId={}, customer={}", req.getOrderId(), customerId);

    var order =
        orderRepository
            .findById(new OrderId(req.getOrderId()))
            .orElseThrow(
                () -> new IllegalArgumentException("Orden no encontrada: " + req.getOrderId()));

    if (!order.getCustomerId().getValue().equals(customerId)) {
      throw new IllegalArgumentException("No tienes permiso para pagar esta orden");
    }

    var customer =
        customerRepository
            .findById(new CustomerId(customerId))
            .orElseThrow(
                () ->
                    new com.eliteshop.colombia.customer.domain.exception.CustomerNotFoundException(
                        "Customer no encontrado"));

    String invoice = "INV-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();

    CheckoutSessionRequest sessionRequest =
        CheckoutSessionRequest.builder()
            .orderId(req.getOrderId())
            .storeName("EliteShop Colombia")
            .amount(order.getTotalAmount().getValue())
            .currency("COP")
            .invoice(invoice)
            .description("Pago de pedido")
            .customerEmail(customer.getEmail().getValue())
            .paymentMethod(req.getPaymentMethod())
            .billing(req.getBilling())
            .build();

    return createCheckoutSessionUseCase
        .execute(sessionRequest)
        .map(
            session ->
                ResponseEntity.ok(
                    CreateCheckoutSessionResponse.builder()
                        .sessionId(session.getSessionId())
                        .token(session.getToken())
                        .invoice(session.getInvoice())
                        .build()))
        .block();
  }

  @PostMapping("/confirm/{refId}")
  public ResponseEntity<ConfirmPaymentResponse> confirmPayment(
      HttpServletRequest request, @PathVariable String refId) {

    UUID customerId = UUID.fromString((String) request.getAttribute("gateway.userId"));

    log.info("Confirmando pago refId={}, customer={}", refId, customerId);

    return confirmPaymentUseCase
        .execute(refId)
        .flatMap(
            payment -> {
              if (payment.getOrderId() == null) {
                return Mono.just(buildConfirmResponse(payment));
              }
              return Mono.justOrEmpty(orderRepository.findById(new OrderId(payment.getOrderId())))
                  .switchIfEmpty(Mono.error(new IllegalArgumentException("Orden no encontrada")))
                  .map(
                      order -> {
                        if (!order.getCustomerId().getValue().equals(customerId)) {
                          throw new IllegalArgumentException(
                              "No tienes permiso para confirmar este pago");
                        }
                        return buildConfirmResponse(payment);
                      });
            })
        .blockOptional()
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/{invoice}")
  public ResponseEntity<PaymentInfoResponse> getPaymentByInvoice(
      HttpServletRequest request, @PathVariable String invoice) {

    UUID customerId = UUID.fromString((String) request.getAttribute("gateway.userId"));

    log.info("Consultando pago por invoice={}, customer={}", invoice, customerId);

    return confirmPaymentUseCase
        .getPaymentByInvoice(invoice)
        .flatMap(
            payment -> {
              if (payment.getOrderId() == null) {
                return Mono.just(buildPaymentInfoResponse(payment));
              }
              return Mono.justOrEmpty(orderRepository.findById(new OrderId(payment.getOrderId())))
                  .switchIfEmpty(Mono.error(new IllegalArgumentException("Orden no encontrada")))
                  .map(
                      order -> {
                        if (!order.getCustomerId().getValue().equals(customerId)) {
                          throw new IllegalArgumentException(
                              "No tienes permiso para consultar este pago");
                        }
                        return buildPaymentInfoResponse(payment);
                      });
            })
        .blockOptional()
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping("/orders/{orderId}/retry")
  public ResponseEntity<?> retryPayment(
      HttpServletRequest request,
      @PathVariable UUID orderId,
      @RequestBody(required = false) RetryPaymentRequest retryRequest) {

    UUID customerId = UUID.fromString((String) request.getAttribute("gateway.userId"));

    log.info(
        "Reintentando pago para orderId={}, customer={}, paymentMethodId={}",
        orderId,
        customerId,
        retryRequest != null ? retryRequest.getPaymentMethodId() : "Smart Checkout");

    var order =
        orderRepository
            .findById(new OrderId(orderId))
            .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada: " + orderId));

    if (!order.getCustomerId().getValue().equals(customerId)) {
      throw new IllegalArgumentException("No tienes permiso para reintentar el pago de esta orden");
    }

    if (retryRequest != null && retryRequest.getPaymentMethodId() != null) {
      return retryWithSavedCardUseCase
          .execute(orderId, retryRequest.getPaymentMethodId(), retryRequest.getCvv())
          .map(
              payment ->
                  ResponseEntity.ok(
                      RetryPaymentResponse.builder()
                          .status(payment.getStatus().name())
                          .invoice(payment.getInvoice() != null ? payment.getInvoice() : "")
                          .refId(payment.getEpaycoRefId() != null ? payment.getEpaycoRefId() : "")
                          .build()))
          .blockOptional()
          .orElse(ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST).build());
    }

    return retryPaymentUseCase
        .execute(orderId)
        .map(
            session ->
                ResponseEntity.ok(
                    CreateCheckoutSessionResponse.builder()
                        .sessionId(session.getSessionId())
                        .token(session.getToken())
                        .invoice(session.getInvoice())
                        .build()))
        .blockOptional()
        .orElse(ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST).build());
  }

  private ResponseEntity<ConfirmPaymentResponse> buildConfirmResponse(Payment payment) {
    return ResponseEntity.ok(
        ConfirmPaymentResponse.builder()
            .status(payment.getStatus().name())
            .refId(payment.getEpaycoRefId())
            .invoice(payment.getInvoice())
            .build());
  }

  private ResponseEntity<PaymentInfoResponse> buildPaymentInfoResponse(Payment payment) {
    return ResponseEntity.ok(
        PaymentInfoResponse.builder()
            .status(payment.getStatus().name())
            .invoice(payment.getInvoice() != null ? payment.getInvoice() : "")
            .orderId(payment.getOrderId() != null ? payment.getOrderId().toString() : "")
            .build());
  }
}
