package com.eliteshop.colombia.order.infrastructure.controller.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderRequest {

  @NotNull(message = "El ID del cliente es requerido")
  private UUID customerId;

  @NotNull(message = "El monto total es requerido")
  private BigDecimal totalAmount;

  @NotNull(message = "La dirección de envío es requerida")
  private String shippingAddress;

  @NotNull(message = "El departamento de envío es requerido")
  private String shippingDepartment;

  @NotNull(message = "La ciudad de envío es requerida")
  private String shippingCity;

  private String status;
}
