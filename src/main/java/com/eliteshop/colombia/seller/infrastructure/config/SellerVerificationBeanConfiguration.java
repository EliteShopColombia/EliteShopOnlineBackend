package com.eliteshop.colombia.seller.infrastructure.config;

import com.eliteshop.colombia.seller.application.usecase.VerifySellerUseCase;
import com.eliteshop.colombia.seller.domain.model.verification.SellerVerificationRepository;
import com.eliteshop.colombia.seller.domain.repository.SellerRepository;
import com.eliteshop.colombia.seller.infrastructure.adapter.FaceMatcherAdapter;
import com.eliteshop.colombia.seller.infrastructure.adapter.MinIOAdapter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class SellerVerificationBeanConfiguration {

  @Bean
  public WebClient sellerVerificationWebClient() {
    return WebClient.builder().build();
  }

  @Bean
  public VerifySellerUseCase verifySellerUseCase(
          SellerVerificationRepository repository,
          SellerRepository sellerRepository,
          MinIOAdapter minIOAdapter,
          FaceMatcherAdapter faceMatcherAdapter,
          ApplicationEventPublisher eventPublisher) {
    return new VerifySellerUseCase(repository, sellerRepository, minIOAdapter, faceMatcherAdapter, eventPublisher);
  }
}