package com.eliteshop.colombia.checkout.infrastructure.controller.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutRequest {

  @NotBlank(message = "La dirección de envío es requerida")
  private String shippingAddress;

  @NotBlank(message = "El departamento de envío es requerido")
  private String shippingDepartment;

  @NotBlank(message = "La ciudad de envío es requerida")
  private String shippingCity;

  private UUID paymentMethodId;

  private String cvv;

  private String cardNumber;

  private Integer expiryMonth;

  private Integer expiryYear;

  private String docType;

  private String docNumber;

  public boolean isNewCard() {
    return paymentMethodId == null;
  }
}
