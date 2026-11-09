package com.eliteshop.colombia.order.infrastructure.controller.dto;

import com.eliteshop.colombia.order.domain.model.DisputeReason;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DisputeRequest {

  @NotNull(message = "El motivo de la disputa es requerido")
  private DisputeReason reason;
}
