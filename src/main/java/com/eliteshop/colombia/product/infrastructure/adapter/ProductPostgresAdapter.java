package com.eliteshop.colombia.product.infrastructure.adapter;

import com.eliteshop.colombia.product.domain.model.Product;
import com.eliteshop.colombia.product.domain.model.ProductId;
import com.eliteshop.colombia.product.domain.model.ProductName;
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

    jpaRepository.save(existingEntity);
  }

  @Override
  public void delete(ProductId id) {
    jpaRepository.deleteById(id.getValue());
  }

  @Override
  public List<Product> findAll() {
    return jpaRepository.findAll().stream().map(mapper::toDomain).collect(Collectors.toList());
  }

  @Override
  public Page<Product> findAll(Pageable pageable) {
    return jpaRepository.findAll(pageable).map(mapper::toDomain);
  }

  @Override
  public Optional<Product> findById(ProductId id) {
    return jpaRepository.findById(id.getValue()).map(mapper::toDomain);
  }

  @Override
  public Optional<Product> findByName(ProductName name) {
    return jpaRepository.findByName(name.getValue()).map(mapper::toDomain);
  }
}
