package com.eliteshop.colombia.cart.infrastructure.controller.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCartItemRequest {

  @NotNull(message = "La cantidad es requerida")
  @Min(value = 1, message = "La cantidad debe ser al menos 1")
  private Integer quantity;
}
