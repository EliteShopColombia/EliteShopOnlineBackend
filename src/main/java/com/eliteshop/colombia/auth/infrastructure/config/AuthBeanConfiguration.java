package com.eliteshop.colombia.auth.infrastructure.config;

import com.eliteshop.colombia.auth.application.LoginUseCase;
import com.eliteshop.colombia.auth.application.RefreshUseCase;
import com.eliteshop.colombia.auth.application.RegisterUseCase;
import com.eliteshop.colombia.auth.infrastructure.filter.JwtAuthFilter;
import com.eliteshop.colombia.customer.domain.repository.CustomerRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class AuthBeanConfiguration {

  @Bean
  public JwtService jwtService(JwtProperties jwtProperties) {
    return new JwtService(jwtProperties);
  }

  @Bean
  public JwtAuthFilter jwtAuthFilter(JwtService jwtService) {
    return new JwtAuthFilter(jwtService);
  }

  @Bean
  public RegisterUseCase registerUseCase(
      CustomerRepository repository, PasswordEncoder passwordEncoder) {
    return new RegisterUseCase(repository, passwordEncoder);
  }

  @Bean
  public LoginUseCase loginUseCase(
      CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
    return new LoginUseCase(customerRepository, passwordEncoder);
  }

  @Bean
  public RefreshUseCase refreshUseCase(
      CustomerRepository customerRepository, JwtService jwtService) {
    return new RefreshUseCase(customerRepository, jwtService);
  }
}
