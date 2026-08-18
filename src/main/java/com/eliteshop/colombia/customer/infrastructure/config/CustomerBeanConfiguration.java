package com.eliteshop.colombia.customer.infrastructure.config;

import com.eliteshop.colombia.customer.application.CustomerDeleteUseCase;
import com.eliteshop.colombia.customer.application.CustomerFindAllUseCase;
import com.eliteshop.colombia.customer.application.CustomerFindByIdUseCase;
import com.eliteshop.colombia.customer.application.CustomerUpdateUseCase;
import com.eliteshop.colombia.customer.domain.repository.CustomerRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerBeanConfiguration {

  @Bean
  public CustomerUpdateUseCase customerUpdateUseCase(CustomerRepository repository) {
    return new CustomerUpdateUseCase(repository);
  }

  @Bean
  public CustomerDeleteUseCase customerDeleteUseCase(CustomerRepository repository) {
    return new CustomerDeleteUseCase(repository);
  }

  @Bean
  public CustomerFindAllUseCase customerFindAllUseCase(CustomerRepository repository) {
    return new CustomerFindAllUseCase(repository);
  }

  @Bean
  public CustomerFindByIdUseCase customerFindByIdUseCase(CustomerRepository repository) {
    return new CustomerFindByIdUseCase(repository);
  }
}
