package com.eliteshop.colombia.admin.infrastructure.controller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SellerStatusRequest {
  @NotNull(message = "El campo isActive es requerido")
  private Boolean isActive;
}
