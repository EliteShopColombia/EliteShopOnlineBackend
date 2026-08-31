package com.eliteshop.colombia.product.infrastructure.controller.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductRequest {

  @NotNull(message = "El ID del vendedor es obligatorio")
  private UUID sellerId;

  @NotBlank(message = "El nombre del producto es obligatorio")
  @Size(min = 2, max = 255, message = "El nombre debe tener entre 2 y 255 caracteres")
  private String name;

  @NotNull(message = "El precio del producto es obligatorio")
  @Positive(message = "El precio debe ser mayor a 0")
  private Double price;

  @NotNull(message = "El stock del producto es obligatorio")
  @Min(value = 0, message = "El stock no puede ser negativo")
  private Integer stock;

  @Size(max = 100, message = "La categoría no puede exceder los 100 caracteres")
  private String category;
}
