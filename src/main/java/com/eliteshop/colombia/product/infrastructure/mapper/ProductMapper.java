package com.eliteshop.colombia.product.infrastructure.mapper;

import com.eliteshop.colombia.product.domain.model.*;
import com.eliteshop.colombia.product.infrastructure.controller.dto.ProductRequest;
import com.eliteshop.colombia.product.infrastructure.controller.dto.ProductResponse;
import com.eliteshop.colombia.product.infrastructure.controller.dto.ProductUpdateRequest;
import com.eliteshop.colombia.product.infrastructure.persistence.ProductEntity;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

  public Product toDomain(ProductEntity entity) {
    if (entity == null) return null;

    return new Product(
        new ProductId(entity.getId()),
        new ProductSellerId(entity.getSellerId()),
        new ProductName(entity.getName()),
        new ProductPrice(entity.getPrice()),
        new ProductStock(entity.getStock()));
  }

  public ProductEntity toEntity(Product domain) {
    if (domain == null) return null;

    ProductEntity entity = new ProductEntity();
    entity.setId(domain.getId().getValue());
    entity.setSellerId(domain.getSellerId().getValue());
    entity.setName(domain.getName().getValue());
    entity.setPrice(domain.getPrice().getValue());
    entity.setStock(domain.getStock().getValue());

    return entity;
  }

  public Product toDomainFromRequest(ProductRequest request) {
    if (request == null) return null;

    return new Product(
        ProductId.generate(),
        new ProductSellerId(request.getSellerId()),
        new ProductName(request.getName()),
        new ProductPrice(BigDecimal.valueOf(request.getPrice())),
        new ProductStock(request.getStock()));
  }

  public Product toDomainFromUpdateRequest(ProductUpdateRequest request, Product existing) {
    if (request == null || existing == null) return null;

    return new Product(
        existing.getId(),
        existing.getSellerId(),
        new ProductName(request.getName()),
        new ProductPrice(BigDecimal.valueOf(request.getPrice())),
        new ProductStock(request.getStock()));
  }

  public ProductResponse toResponse(Product domain) {
    if (domain == null) return null;

    ProductResponse response = new ProductResponse();
    response.setId(domain.getId().getValue());
    response.setSellerId(domain.getSellerId().getValue());
    response.setName(domain.getName().getValue());
    response.setPrice(domain.getPrice().getValue());
    response.setStock(domain.getStock().getValue());

    return response;
  }
}
