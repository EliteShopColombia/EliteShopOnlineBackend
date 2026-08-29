package com.eliteshop.colombia.order.infrastructure.config;

import com.eliteshop.colombia.order.application.AddTrackingEventUseCase;
import com.eliteshop.colombia.order.application.CancelOrderUseCase;
import com.eliteshop.colombia.order.application.CompleteOrderUseCase;
import com.eliteshop.colombia.order.application.ConfirmDeliveryUseCase;
import com.eliteshop.colombia.order.application.DisputeOrderUseCase;
import com.eliteshop.colombia.order.application.FindOrdersByCustomerIdUseCase;
import com.eliteshop.colombia.order.application.FindOrdersBySellerUseCase;
import com.eliteshop.colombia.order.application.GetTrackingEventsUseCase;
import com.eliteshop.colombia.order.application.OrderDeleteUseCase;
import com.eliteshop.colombia.order.application.OrderFindAllUseCase;
import com.eliteshop.colombia.order.application.OrderFindByIdUseCase;
import com.eliteshop.colombia.order.application.OrderSaveUseCase;
import com.eliteshop.colombia.order.application.OrderStatusCountsUseCase;
import com.eliteshop.colombia.order.application.OrderSummaryUseCase;
import com.eliteshop.colombia.order.application.OrderUpdateUseCase;
import com.eliteshop.colombia.order.application.OutForDeliveryUseCase;
import com.eliteshop.colombia.order.application.PrepareOrderUseCase;
import com.eliteshop.colombia.order.application.RefundOrderUseCase;
import com.eliteshop.colombia.order.application.SearchOrdersUseCase;
import com.eliteshop.colombia.order.application.ShipOrderUseCase;
import com.eliteshop.colombia.order.application.UpdateTrackingUseCase;
import com.eliteshop.colombia.order.domain.repository.OrderItemRepository;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import com.eliteshop.colombia.order.domain.repository.TrackingEventRepository;
import com.eliteshop.colombia.product.domain.repository.ProductRepository;
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

  @Bean
  public FindOrdersByCustomerIdUseCase findOrdersByCustomerIdUseCase(OrderRepository repository) {
    return new FindOrdersByCustomerIdUseCase(repository);
  }

  @Bean
  public CancelOrderUseCase cancelOrderUseCase(
      OrderRepository repository,
      OrderItemRepository orderItemRepository,
      ProductRepository productRepository,
      ApplicationEventPublisher eventPublisher) {
    return new CancelOrderUseCase(
        repository, orderItemRepository, productRepository, eventPublisher);
  }

  @Bean
  public ConfirmDeliveryUseCase confirmDeliveryUseCase(
      OrderRepository repository, ApplicationEventPublisher eventPublisher) {
    return new ConfirmDeliveryUseCase(repository, eventPublisher);
  }

  @Bean
  public PrepareOrderUseCase prepareOrderUseCase(
      OrderRepository repository, ApplicationEventPublisher eventPublisher) {
    return new PrepareOrderUseCase(repository, eventPublisher);
  }

  @Bean
  public ShipOrderUseCase shipOrderUseCase(
      OrderRepository repository, ApplicationEventPublisher eventPublisher) {
    return new ShipOrderUseCase(repository, eventPublisher);
  }

  @Bean
  public OutForDeliveryUseCase outForDeliveryUseCase(
      OrderRepository repository, ApplicationEventPublisher eventPublisher) {
    return new OutForDeliveryUseCase(repository, eventPublisher);
  }

  @Bean
  public UpdateTrackingUseCase updateTrackingUseCase(OrderRepository repository) {
    return new UpdateTrackingUseCase(repository);
  }

  @Bean
  public CompleteOrderUseCase completeOrderUseCase(
      OrderRepository repository, ApplicationEventPublisher eventPublisher) {
    return new CompleteOrderUseCase(repository, eventPublisher);
  }

  @Bean
  public DisputeOrderUseCase disputeOrderUseCase(
      OrderRepository repository, ApplicationEventPublisher eventPublisher) {
    return new DisputeOrderUseCase(repository, eventPublisher);
  }

  @Bean
  public FindOrdersBySellerUseCase findOrdersBySellerUseCase(OrderRepository orderRepository) {
    return new FindOrdersBySellerUseCase(orderRepository);
  }

  @Bean
  public RefundOrderUseCase refundOrderUseCase(
      OrderRepository repository,
      OrderItemRepository orderItemRepository,
      ProductRepository productRepository,
      ApplicationEventPublisher eventPublisher) {
    return new RefundOrderUseCase(
        repository, orderItemRepository, productRepository, eventPublisher);
  }

  @Bean
  public OrderSummaryUseCase orderSummaryUseCase(
      OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
    return new OrderSummaryUseCase(orderRepository, orderItemRepository);
  }

  @Bean
  public AddTrackingEventUseCase addTrackingEventUseCase(
      TrackingEventRepository trackingEventRepository, OrderRepository orderRepository) {
    return new AddTrackingEventUseCase(trackingEventRepository, orderRepository);
  }

  @Bean
  public GetTrackingEventsUseCase getTrackingEventsUseCase(
      TrackingEventRepository trackingEventRepository) {
    return new GetTrackingEventsUseCase(trackingEventRepository);
  }

  @Bean
  public SearchOrdersUseCase searchOrdersUseCase(
      OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
    return new SearchOrdersUseCase(orderRepository, orderItemRepository);
  }

  @Bean
  public OrderStatusCountsUseCase orderStatusCountsUseCase(
      OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
    return new OrderStatusCountsUseCase(orderRepository, orderItemRepository);
  }
}
