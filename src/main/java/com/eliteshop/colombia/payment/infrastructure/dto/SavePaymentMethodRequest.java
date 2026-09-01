package com.eliteshop.colombia.payment.infrastructure.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SavePaymentMethodRequest {

  @NotBlank(message = "El número de tarjeta es requerido")
  private String cardNumber;

  @NotNull(message = "El mes de vencimiento es requerido")
  private Integer expiryMonth;

  @NotNull(message = "El año de vencimiento es requerido")
  private Integer expiryYear;

  @NotBlank(message = "El CVV es requerido")
  @Size(min = 3, max = 4, message = "El CVV debe tener entre 3 y 4 dígitos")
  private String cvc;

  private String docType;

  private String docNumber;

  private boolean setDefault;
}
