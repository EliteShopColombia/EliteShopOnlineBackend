package com.eliteshop.colombia.product.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "product")
public class ProductEntity {

  @Id
  @Column(name = "product_id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "seller_id", nullable = false)
  private UUID sellerId;

  @Column(name = "product_name", nullable = false, unique = true, length = 255)
  private String name;

  @Column(name = "product_price", nullable = false, precision = 12, scale = 2)
  private BigDecimal price;

  @Column(name = "product_stock", nullable = false)
  private Integer stock;
}
