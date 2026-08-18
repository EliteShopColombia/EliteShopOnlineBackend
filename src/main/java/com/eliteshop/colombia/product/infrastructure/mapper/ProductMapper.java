package com.eliteshop.colombia.product.infrastructure.mapper;

import com.eliteshop.colombia.product.domain.model.*;
import com.eliteshop.colombia.product.infrastructure.controller.dto.ProductRequest;
import com.eliteshop.colombia.product.infrastructure.controller.dto.ProductResponse;
import com.eliteshop.colombia.product.infrastructure.controller.dto.ProductUpdateRequest;
import com.eliteshop.colombia.product.infrastructure.persistence.ProductEntity;
import com.eliteshop.colombia.product.infrastructure.persistence.ProductImageEntity;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;
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
        new ProductStock(entity.getStock()),
        new ArrayList<>());
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
        new ProductStock(request.getStock()),
        new ArrayList<>());
  }

  public Product toDomainFromUpdateRequest(ProductUpdateRequest request, Product existing) {
    if (request == null || existing == null) return null;

    return new Product(
        existing.getId(),
        existing.getSellerId(),
        new ProductName(request.getName()),
        new ProductPrice(BigDecimal.valueOf(request.getPrice())),
        new ProductStock(request.getStock()),
        existing.getImages());
  }

  public ProductResponse toResponse(Product domain) {
    if (domain == null) return null;

    ProductResponse response = new ProductResponse();
    response.setId(domain.getId().getValue());
    response.setSellerId(domain.getSellerId().getValue());
    response.setName(domain.getName().getValue());
    response.setPrice(domain.getPrice().getValue());
    response.setStock(domain.getStock().getValue());
    if (domain.getImages() != null) {
      response.setImages(
          domain.getImages().stream()
              .map(img -> img.getImageUrl().getValue())
              .collect(Collectors.toList()));
    } else {
      response.setImages(Collections.emptyList());
    }

    return response;
  }

  public ProductImage toDomain(ProductImageEntity entity) {
    if (entity == null) return null;
    return new ProductImage(
        new ProductImageId(entity.getId()),
        new ProductId(entity.getProductId()),
        new ProductImageUrl(entity.getImageUrl()),
        new ProductImageOrder(entity.getOrder()));
  }

  public ProductImageEntity toEntity(ProductImage domain) {
    if (domain == null) return null;
    ProductImageEntity entity = new ProductImageEntity();
    entity.setId(domain.getId().getValue());
    entity.setProductId(domain.getProductId().getValue());
    entity.setImageUrl(domain.getImageUrl().getValue());
    entity.setOrder(domain.getOrder().getValue());
    return entity;
  }
}
