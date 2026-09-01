package com.eliteshop.colombia.product.infrastructure.adapter;

import com.eliteshop.colombia.product.domain.model.ProductId;
import com.eliteshop.colombia.product.domain.model.ProductImage;
import com.eliteshop.colombia.product.domain.model.ProductImageId;
import com.eliteshop.colombia.product.domain.model.ProductImageOrder;
import com.eliteshop.colombia.product.domain.model.ProductImageUrl;
import com.eliteshop.colombia.product.domain.repository.ProductImageRepository;
import com.eliteshop.colombia.product.infrastructure.persistence.ProductImageEntity;
import com.eliteshop.colombia.product.infrastructure.persistence.ProductImageJpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductImagePostgresAdapter implements ProductImageRepository {

  private final ProductImageJpaRepository jpaRepository;

  @Override
  public List<ProductImage> findByProductId(ProductId productId) {
    return jpaRepository.findByProductIdOrderByOrderAsc(productId.getValue()).stream()
        .map(this::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public ProductImage save(ProductImage image) {
    ProductImageEntity entity = toEntity(image);
    ProductImageEntity savedEntity = jpaRepository.save(entity);
    return toDomain(savedEntity);
  }

  @Override
  public void deleteByProductId(ProductId productId) {
    jpaRepository.deleteByProductId(productId.getValue());
  }

  private ProductImage toDomain(ProductImageEntity entity) {
    if (entity == null) return null;
    return new ProductImage(
        new ProductImageId(entity.getId()),
        new ProductId(entity.getProductId()),
        new ProductImageUrl(entity.getImageUrl()),
        new ProductImageOrder(entity.getOrder()));
  }

  private ProductImageEntity toEntity(ProductImage image) {
    if (image == null) return null;
    ProductImageEntity entity = new ProductImageEntity();
    entity.setId(image.getId().getValue());
    entity.setProductId(image.getProductId().getValue());
    entity.setImageUrl(image.getImageUrl().getValue());
    entity.setOrder(image.getOrder().getValue());
    entity.setCreatedAt(LocalDateTime.now());
    return entity;
  }
}
