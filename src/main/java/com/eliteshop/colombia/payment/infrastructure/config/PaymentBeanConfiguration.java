package com.eliteshop.colombia.payment.infrastructure.config;

import com.eliteshop.colombia.customer.domain.repository.CustomerRepository;
import com.eliteshop.colombia.order.application.OrderUpdateUseCase;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import com.eliteshop.colombia.payment.application.usecase.*;
import com.eliteshop.colombia.payment.domain.port.*;
import java.util.Base64;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(EpaycoProperties.class)
public class PaymentBeanConfiguration {

  @Bean
  public CreateCheckoutSessionUseCase createCheckoutSessionUseCase(
      PaymentGateway paymentGateway,
      PaymentRepository paymentRepository,
      EpaycoProperties epaycoProperties) {
    return new CreateCheckoutSessionUseCase(paymentGateway, paymentRepository, epaycoProperties);
  }

  @Bean
  public ConfirmPaymentUseCase confirmPaymentUseCase(
      PaymentGateway paymentGateway, PaymentRepository paymentRepository) {
    return new ConfirmPaymentUseCase(paymentGateway, paymentRepository);
  }

  @Bean
  public SavePaymentMethodUseCase savePaymentMethodUseCase(
      PaymentGateway paymentGateway,
      CustomerPaymentMethodRepository customerPaymentMethodRepository,
      com.eliteshop.colombia.customer.domain.repository.CustomerRepository customerRepository) {
    return new SavePaymentMethodUseCase(
        paymentGateway, customerPaymentMethodRepository, customerRepository);
  }

  @Bean
  public GetPaymentMethodsUseCase getPaymentMethodsUseCase(
      CustomerPaymentMethodRepository customerPaymentMethodRepository) {
    return new GetPaymentMethodsUseCase(customerPaymentMethodRepository);
  }

  @Bean
  public DeletePaymentMethodUseCase deletePaymentMethodUseCase(
      CustomerPaymentMethodRepository customerPaymentMethodRepository) {
    return new DeletePaymentMethodUseCase(customerPaymentMethodRepository);
  }

  @Bean
  public SetDefaultPaymentMethodUseCase setDefaultPaymentMethodUseCase(
      CustomerPaymentMethodRepository customerPaymentMethodRepository) {
    return new SetDefaultPaymentMethodUseCase(customerPaymentMethodRepository);
  }

  @Bean
  public RetryPaymentUseCase retryPaymentUseCase(
      PaymentGateway paymentGateway,
      PaymentRepository paymentRepository,
      OrderRepository orderRepository,
      EpaycoProperties epaycoProperties) {
    return new RetryPaymentUseCase(
        paymentGateway, paymentRepository, orderRepository, epaycoProperties);
  }

  @Bean
  public RetryWithSavedCardUseCase retryWithSavedCardUseCase(
      PaymentGateway paymentGateway,
      PaymentRepository paymentRepository,
      OrderRepository orderRepository,
      CustomerPaymentMethodRepository paymentMethodRepository,
      CustomerRepository customerRepository,
      OrderUpdateUseCase orderUpdateUseCase) {
    return new RetryWithSavedCardUseCase(
        paymentGateway,
        paymentRepository,
        orderRepository,
        paymentMethodRepository,
        customerRepository,
        orderUpdateUseCase);
  }

  @Bean
  public WebClient apifyWebClient(EpaycoProperties properties) {

    String credentials = properties.getPublicKey() + ":" + properties.getPrivateKey();
    String basicAuth = Base64.getEncoder().encodeToString(credentials.getBytes());

    ExchangeStrategies strategies =
        ExchangeStrategies.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
            .build();

    return WebClient.builder()
        .baseUrl(properties.getApifyBaseUrl())
        .defaultHeader("Authorization", "Basic " + basicAuth)
        .defaultHeader("Content-Type", "application/json")
        .exchangeStrategies(strategies)
        .build();
  }

  @Bean
  public WebClient classicWebClient(EpaycoProperties properties) {

    ExchangeStrategies strategies =
        ExchangeStrategies.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
            .build();

    return WebClient.builder()
        .baseUrl(properties.getApiBaseUrl())
        .defaultHeader("Content-Type", "application/json")
        .defaultHeader("Accept", "application/json")
        .defaultHeader("Type", "sdk-jwt")
        .defaultHeader("lang", "java")
        .exchangeStrategies(strategies)
        .build();
  }
}
