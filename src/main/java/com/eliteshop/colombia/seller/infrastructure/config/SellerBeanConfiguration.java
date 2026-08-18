package com.eliteshop.colombia.seller.infrastructure.config;

import com.eliteshop.colombia.seller.application.usecase.*;
import com.eliteshop.colombia.seller.domain.repository.SellerRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SellerBeanConfiguration {

  @Bean
  public SellerDeleteUseCase sellerDeleteUseCase(SellerRepository sellerRepository) {
    return new SellerDeleteUseCase(sellerRepository);
  }

  @Bean
  public SellerFindAllUseCase sellerFindAllUseCase(SellerRepository sellerRepository) {
    return new SellerFindAllUseCase(sellerRepository);
  }

  @Bean
  public SellerFindByDniUseCase sellerFindByDniUseCase(SellerRepository sellerRepository) {
    return new SellerFindByDniUseCase(sellerRepository);
  }

  @Bean
  public SellerFindByIdUseCase sellerFindByIdUseCase(SellerRepository sellerRepository) {
    return new SellerFindByIdUseCase(sellerRepository);
  }

  @Bean
  public SellerFindBankInfoBySellerIdUseCase sellerFindBankInfoBySellerIdUseCase(
      SellerFindByIdUseCase sellerFindByIdUseCase) {
    return new SellerFindBankInfoBySellerIdUseCase(sellerFindByIdUseCase);
  }

  @Bean
  public SellerFindContactBySellerIdUseCase sellerFindContactBySellerIdUseCase(
      SellerFindByIdUseCase sellerFindByIdUseCase) {
    return new SellerFindContactBySellerIdUseCase(sellerFindByIdUseCase);
  }

  @Bean
  public SellerSaveUseCase sellerSaveUseCase(SellerRepository sellerRepository) {
    return new SellerSaveUseCase(sellerRepository);
  }

  @Bean
  public SellerUpdateUseCase sellerUpdateUseCase(SellerRepository sellerRepository) {
    return new SellerUpdateUseCase(sellerRepository);
  }
}
