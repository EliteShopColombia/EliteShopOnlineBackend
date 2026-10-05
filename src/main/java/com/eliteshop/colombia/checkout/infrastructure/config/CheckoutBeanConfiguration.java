package com.eliteshop.colombia.checkout.infrastructure.config;

import com.eliteshop.colombia.cart.domain.repository.CartRepository;
import com.eliteshop.colombia.checkout.application.CheckoutUseCase;
import com.eliteshop.colombia.checkout.application.OrderPersister;
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

  /**
   * Bean separado para que {@code @Transactional} de {@link OrderPersister} pase por el proxy de
   * Spring; si el método viviera en CheckoutUseCase la transacción no se abriría (self-invocation).
   */
  @Bean
  public OrderPersister orderPersister(
      OrderRepository orderRepository,
      OrderItemRepository orderItemRepository,
      ProductRepository productRepository,
      PaymentRepository paymentRepository) {
    return new OrderPersister(
        orderRepository, orderItemRepository, productRepository, paymentRepository);
  }

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
      LocationValidationService locationValidationService,
      OrderPersister orderPersister) {
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
        locationValidationService,
        orderPersister);
  }
}
