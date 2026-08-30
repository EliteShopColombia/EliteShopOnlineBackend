package com.eliteshop.colombia.checkout.infrastructure.config;

import com.eliteshop.colombia.cart.domain.repository.CartRepository;
import com.eliteshop.colombia.checkout.application.CheckoutUseCase;
import com.eliteshop.colombia.customer.domain.repository.CustomerRepository;
import com.eliteshop.colombia.order.application.OrderUpdateUseCase;
import com.eliteshop.colombia.order.domain.repository.OrderItemRepository;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import com.eliteshop.colombia.payment.domain.port.CustomerPaymentMethodRepository;
import com.eliteshop.colombia.payment.domain.port.PaymentGateway;
import com.eliteshop.colombia.payment.domain.port.PaymentRepository;
import com.eliteshop.colombia.product.domain.repository.ProductRepository;
import com.eliteshop.colombia.seller.domain.repository.SellerRepository;
import com.eliteshop.colombia.shared.domain.LocationValidationService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CheckoutBeanConfiguration {

  @Bean
  public CheckoutUseCase checkoutUseCase(
      CartRepository cartRepository,
      ProductRepository productRepository,
      OrderRepository orderRepository,
      OrderItemRepository orderItemRepository,
      PaymentRepository paymentRepository,
      CustomerPaymentMethodRepository paymentMethodRepository,
      CustomerRepository customerRepository,
      PaymentGateway paymentGateway,
      OrderUpdateUseCase orderUpdateUseCase,
      ApplicationEventPublisher eventPublisher,
      SellerRepository sellerRepository,
      LocationValidationService locationValidationService) {
    return new CheckoutUseCase(
        cartRepository,
        productRepository,
        orderRepository,
        orderItemRepository,
        paymentRepository,
        paymentMethodRepository,
        customerRepository,
        paymentGateway,
        orderUpdateUseCase,
        eventPublisher,
        sellerRepository,
        locationValidationService);
  }
}
