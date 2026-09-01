package com.eliteshop.colombia.checkout.infrastructure.controller;

import com.eliteshop.colombia.checkout.application.CheckoutUseCase;
import com.eliteshop.colombia.checkout.application.CheckoutUseCase.CheckoutRequestFields;
import com.eliteshop.colombia.checkout.application.CheckoutUseCase.CheckoutResult;
import com.eliteshop.colombia.checkout.infrastructure.controller.dto.CheckoutRequest;
import com.eliteshop.colombia.checkout.infrastructure.controller.dto.CheckoutResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/checkout")
@RequiredArgsConstructor
public class CheckoutController {

  private final CheckoutUseCase checkoutUseCase;

  @PostMapping
  public ResponseEntity<CheckoutResponse> checkout(
      HttpServletRequest request, @Valid @RequestBody CheckoutRequest checkoutRequest) {
    UUID customerId = UUID.fromString((String) request.getAttribute("gateway.userId"));

    CheckoutResult result =
        checkoutUseCase.execute(
            customerId,
            new CheckoutRequestFields(
                checkoutRequest.getShippingAddress(),
                checkoutRequest.getShippingDepartment(),
                checkoutRequest.getShippingCity(),
                checkoutRequest.getPaymentMethodId(),
                checkoutRequest.getCvv(),
                checkoutRequest.getCardNumber(),
                checkoutRequest.getExpiryMonth(),
                checkoutRequest.getExpiryYear(),
                checkoutRequest.getDocType(),
                checkoutRequest.getDocNumber()));

    CheckoutResponse response = new CheckoutResponse();
    response.setOrderId(result.orderId);
    response.setPaymentId(result.paymentId);
    response.setStatus(result.status);
    response.setEpaycoRefId(result.epaycoRefId);
    response.setInvoice(result.invoice);
    response.setTotalAmount(result.totalAmount);

    return ResponseEntity.ok(response);
  }
}
