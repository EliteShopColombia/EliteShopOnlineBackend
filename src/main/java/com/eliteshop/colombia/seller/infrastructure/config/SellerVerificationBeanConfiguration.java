package com.eliteshop.colombia.seller.infrastructure.config;

import com.eliteshop.colombia.seller.application.usecase.VerifySellerUseCase;
import com.eliteshop.colombia.seller.domain.model.verification.SellerVerificationRepository;
import com.eliteshop.colombia.seller.domain.repository.SellerRepository;
import com.eliteshop.colombia.seller.infrastructure.adapter.FaceMatcherAdapter;
import com.eliteshop.colombia.seller.infrastructure.adapter.MinIOAdapter;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(FaceMatcherProperties.class)
public class SellerVerificationBeanConfiguration {

  @Bean
  public WebClient sellerVerificationWebClient() {
    return WebClient.builder().build();
  }

  @Bean
  public ThreadPoolTaskExecutor sellerVerificationExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(4);
    executor.setQueueCapacity(10);
    executor.setThreadNamePrefix("seller-verification-");
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    executor.initialize();
    return executor;
  }

  @Bean
  public VerifySellerUseCase verifySellerUseCase(
      SellerVerificationRepository repository,
      SellerRepository sellerRepository,
      MinIOAdapter minIOAdapter,
      FaceMatcherAdapter faceMatcherAdapter,
      ApplicationEventPublisher eventPublisher) {
    return new VerifySellerUseCase(
        repository, sellerRepository, minIOAdapter, faceMatcherAdapter, eventPublisher);
  }
}
