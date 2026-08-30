package com.eliteshop.colombia.admin.infrastructure.config;

import com.eliteshop.colombia.admin.application.AdminRegisterUseCase;
import com.eliteshop.colombia.customer.domain.repository.CustomerRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminBeanConfiguration {

  @Bean
  public AdminRegisterUseCase adminRegisterUseCase(
      CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
    return new AdminRegisterUseCase(customerRepository, passwordEncoder);
  }
}
