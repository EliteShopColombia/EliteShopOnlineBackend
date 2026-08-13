package com.eliteshop.colombia.product.infrastructure.config;

import com.eliteshop.colombia.product.application.usecase.*;
import com.eliteshop.colombia.product.domain.repository.ProductRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProductBeanConfiguration {

  @Bean
  public ProductSaveUseCase productSaveUseCase(ProductRepository repository) {
    return new ProductSaveUseCase(repository);
  }

  @Bean
  public ProductUpdateUseCase productUpdateUseCase(ProductRepository repository) {
    return new ProductUpdateUseCase(repository);
  }

  @Bean
  public ProductDeleteUseCase productDeleteUseCase(ProductRepository repository) {
    return new ProductDeleteUseCase(repository);
  }

  @Bean
  public ProductFindAllUseCase productFindAllUseCase(ProductRepository repository) {
    return new ProductFindAllUseCase(repository);
  }

  @Bean
  public ProductFindByIdUseCase productFindByIdUseCase(ProductRepository repository) {
    return new ProductFindByIdUseCase(repository);
  }
}
