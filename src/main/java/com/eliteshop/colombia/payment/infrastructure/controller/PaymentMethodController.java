package com.eliteshop.colombia.payment.infrastructure.controller;

import com.eliteshop.colombia.payment.application.usecase.*;
import com.eliteshop.colombia.payment.domain.model.paymentmethod.CustomerPaymentMethod;
import com.eliteshop.colombia.payment.infrastructure.dto.PaymentMethodResponse;
import com.eliteshop.colombia.payment.infrastructure.dto.SavePaymentMethodRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payment-methods")
@RequiredArgsConstructor
public class PaymentMethodController {

  private final SavePaymentMethodUseCase savePaymentMethodUseCase;
  private final GetPaymentMethodsUseCase getPaymentMethodsUseCase;
  private final DeletePaymentMethodUseCase deletePaymentMethodUseCase;
  private final SetDefaultPaymentMethodUseCase setDefaultPaymentMethodUseCase;

  @GetMapping
  public ResponseEntity<List<PaymentMethodResponse>> getMethods(HttpServletRequest request) {
    UUID customerId = currentCustomerId(request);
    List<CustomerPaymentMethod> methods = getPaymentMethodsUseCase.execute(customerId);
    List<PaymentMethodResponse> responses =
        methods.stream().map(this::toResponse).collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  @PostMapping
  public ResponseEntity<PaymentMethodResponse> saveMethod(
      HttpServletRequest request, @Valid @RequestBody SavePaymentMethodRequest saveRequest) {
    UUID customerId = currentCustomerId(request);
    return savePaymentMethodUseCase
        .execute(
            customerId,
            saveRequest.getCardNumber(),
            saveRequest.getCvc(),
            saveRequest.getExpiryMonth(),
            saveRequest.getExpiryYear(),
            saveRequest.getDocType(),
            saveRequest.getDocNumber(),
            saveRequest.isSetDefault())
        .map(method -> ResponseEntity.status(HttpStatus.CREATED).body(toResponse(method)))
        .block();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteMethod(HttpServletRequest request, @PathVariable UUID id) {
    UUID customerId = currentCustomerId(request);
    deletePaymentMethodUseCase.execute(customerId, id);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{id}/default")
  public ResponseEntity<PaymentMethodResponse> setDefault(
      HttpServletRequest request, @PathVariable UUID id) {
    UUID customerId = currentCustomerId(request);
    CustomerPaymentMethod method = setDefaultPaymentMethodUseCase.execute(customerId, id);
    return ResponseEntity.ok(toResponse(method));
  }

  private UUID currentCustomerId(HttpServletRequest request) {
    return UUID.fromString((String) request.getAttribute("gateway.userId"));
  }

  private PaymentMethodResponse toResponse(CustomerPaymentMethod method) {
    PaymentMethodResponse response = new PaymentMethodResponse();
    response.setId(method.getId().getValue());
    response.setLast4(method.getLast4().getValue());
    response.setBrand(method.getBrand().getValue());
    response.setExpiryMonth(method.getExpiryMonth().getValue());
    response.setExpiryYear(method.getExpiryYear().getValue());
    response.setDefault(method.isDefault());
    return response;
  }
}
