package com.eliteshop.colombia.product.infrastructure.adapter;

import com.eliteshop.colombia.product.domain.model.Product;
import com.eliteshop.colombia.product.domain.model.ProductId;
import com.eliteshop.colombia.product.domain.model.ProductImage;
import com.eliteshop.colombia.product.domain.model.ProductName;
import com.eliteshop.colombia.product.domain.repository.ProductImageRepository;
import com.eliteshop.colombia.product.domain.repository.ProductRepository;
import com.eliteshop.colombia.product.infrastructure.mapper.ProductMapper;
import com.eliteshop.colombia.product.infrastructure.persistence.ProductEntity;
import com.eliteshop.colombia.product.infrastructure.persistence.ProductJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductPostgresAdapter implements ProductRepository {

  private final ProductJpaRepository jpaRepository;
  private final ProductMapper mapper;
  private final ProductImageRepository productImageRepository;

  @Override
  public Product save(Product product) {
    ProductEntity entity = mapper.toEntity(product);
    ProductEntity savedEntity = jpaRepository.save(entity);
    return mapper.toDomain(savedEntity);
  }

  @Override
  public void update(Product product) {
    ProductEntity existingEntity = jpaRepository.findById(product.getId().getValue()).orElseThrow();

    existingEntity.setName(product.getName().getValue());
    existingEntity.setPrice(product.getPrice().getValue());
    existingEntity.setStock(product.getStock().getValue());
    existingEntity.setCategory(
        product.getCategory() != null ? product.getCategory().getValue() : null);

    jpaRepository.save(existingEntity);
  }

  @Override
  public void delete(ProductId id) {
    jpaRepository.deleteById(id.getValue());
  }

  @Override
  public List<Product> findAll() {
    return jpaRepository.findAll().stream()
        .map(this::toDomainWithImages)
        .collect(Collectors.toList());
  }

  @Override
  public Page<Product> findAll(Pageable pageable) {
    return jpaRepository.findAll(pageable).map(this::toDomainWithImages);
  }

  @Override
  public Optional<Product> findById(ProductId id) {
    return jpaRepository.findById(id.getValue()).map(this::toDomainWithImages);
  }

  @Override
  public Optional<Product> findByName(ProductName name) {
    return jpaRepository.findByName(name.getValue()).map(this::toDomainWithImages);
  }

  @Override
  public void reduceStock(ProductId id, int quantity) {
    int updated = jpaRepository.reduceStock(id.getValue(), quantity);
    if (updated == 0) {
      String name =
          jpaRepository.findById(id.getValue()).map(ProductEntity::getName).orElse("desconocido");
      throw new com.eliteshop.colombia.product.domain.exception.StockInsufficientException(
          "Stock insuficiente para el producto " + name);
    }
  }

  @Override
  public void restoreStock(ProductId id, int quantity) {
    jpaRepository.restoreStock(id.getValue(), quantity);
  }

  private Product toDomainWithImages(ProductEntity entity) {
    if (entity == null) return null;
    ProductId productId = new ProductId(entity.getId());
    List<ProductImage> images = productImageRepository.findByProductId(productId);
    com.eliteshop.colombia.product.domain.model.ProductCategory category = null;
    if (entity.getCategory() != null && !entity.getCategory().isBlank()) {
      category =
          new com.eliteshop.colombia.product.domain.model.ProductCategory(entity.getCategory());
    }
    return new Product(
        productId,
        new com.eliteshop.colombia.product.domain.model.ProductSellerId(entity.getSellerId()),
        new ProductName(entity.getName()),
        new com.eliteshop.colombia.product.domain.model.ProductPrice(entity.getPrice()),
        new com.eliteshop.colombia.product.domain.model.ProductStock(entity.getStock()),
        category,
        images);
  }
}
