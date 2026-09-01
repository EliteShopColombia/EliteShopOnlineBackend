package com.eliteshop.colombia.cart.infrastructure.config;

import com.eliteshop.colombia.cart.application.*;
import com.eliteshop.colombia.cart.domain.repository.CartRepository;
import com.eliteshop.colombia.product.domain.repository.ProductRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CartBeanConfiguration {

  @Bean
  public GetCartUseCase getCartUseCase(CartRepository cartRepository) {
    return new GetCartUseCase(cartRepository);
  }

  @Bean
  public AddToCartUseCase addToCartUseCase(
      CartRepository cartRepository, ProductRepository productRepository) {
    return new AddToCartUseCase(cartRepository, productRepository);
  }

  @Bean
  public UpdateCartItemUseCase updateCartItemUseCase(
      CartRepository cartRepository, ProductRepository productRepository) {
    return new UpdateCartItemUseCase(cartRepository, productRepository);
  }

  @Bean
  public RemoveFromCartUseCase removeFromCartUseCase(CartRepository cartRepository) {
    return new RemoveFromCartUseCase(cartRepository);
  }

  @Bean
  public ClearCartUseCase clearCartUseCase(CartRepository cartRepository) {
    return new ClearCartUseCase(cartRepository);
  }
}
