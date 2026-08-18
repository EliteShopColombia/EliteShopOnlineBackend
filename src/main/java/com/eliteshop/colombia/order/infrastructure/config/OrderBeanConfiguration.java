package com.eliteshop.colombia.order.infrastructure.config;

import com.eliteshop.colombia.order.application.OrderDeleteUseCase;
import com.eliteshop.colombia.order.application.OrderFindAllUseCase;
import com.eliteshop.colombia.order.application.OrderFindByIdUseCase;
import com.eliteshop.colombia.order.application.OrderSaveUseCase;
import com.eliteshop.colombia.order.application.OrderUpdateUseCase;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderBeanConfiguration {

  @Bean
  public OrderSaveUseCase orderSaveUseCase(
      OrderRepository repository, ApplicationEventPublisher eventPublisher) {
    return new OrderSaveUseCase(repository, eventPublisher);
  }

  @Bean
  public OrderUpdateUseCase orderUpdateUseCase(
      OrderRepository repository, ApplicationEventPublisher eventPublisher) {
    return new OrderUpdateUseCase(repository, eventPublisher);
  }

  @Bean
  public OrderDeleteUseCase orderDeleteUseCase(OrderRepository repository) {
    return new OrderDeleteUseCase(repository);
  }

  @Bean
  public OrderFindAllUseCase orderFindAllUseCase(OrderRepository repository) {
    return new OrderFindAllUseCase(repository);
  }

  @Bean
  public OrderFindByIdUseCase orderFindByIdUseCase(OrderRepository repository) {
    return new OrderFindByIdUseCase(repository);
  }
}
