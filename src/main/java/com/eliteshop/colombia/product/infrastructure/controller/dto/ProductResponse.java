package com.eliteshop.colombia.product.infrastructure.controller.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductResponse {

  private UUID id;
  private UUID sellerId;
  private String name;
  private BigDecimal price;
  private Integer stock;
  private List<String> images;
}
