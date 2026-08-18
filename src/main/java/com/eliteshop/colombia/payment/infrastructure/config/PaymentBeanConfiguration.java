package com.eliteshop.colombia.payment.infrastructure.config;

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
      PaymentGateway paymentGateway, PaymentRepository paymentRepository) {
    return new CreateCheckoutSessionUseCase(paymentGateway, paymentRepository);
  }

  @Bean
  public ConfirmPaymentUseCase confirmPaymentUseCase(
      PaymentGateway paymentGateway, PaymentRepository paymentRepository) {
    return new ConfirmPaymentUseCase(paymentGateway, paymentRepository);
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
}
