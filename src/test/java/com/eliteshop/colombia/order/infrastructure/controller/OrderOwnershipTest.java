package com.eliteshop.colombia.order.infrastructure.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eliteshop.colombia.order.application.*;
import com.eliteshop.colombia.order.domain.model.*;
import com.eliteshop.colombia.order.domain.repository.OrderItemRepository;
import com.eliteshop.colombia.order.infrastructure.controller.dto.OrderResponse;
import com.eliteshop.colombia.order.infrastructure.mapper.OrderMapper;
import com.eliteshop.colombia.shared.security.AuthorizationService;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class OrderOwnershipTest {

  private MockMvc mockMvc;
  private OrderFindByIdUseCase findByIdUseCase;
  private CancelOrderUseCase cancelUseCase;
  private AuthorizationService authorizationService;
  private OrderMapper mapper;
  private OrderItemRepository orderItemRepository;

  private final UUID customerId = UUID.randomUUID();
  private final UUID otherCustomerId = UUID.randomUUID();
  private final UUID orderId = UUID.randomUUID();
  private final UUID sellerId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    findByIdUseCase = mock(OrderFindByIdUseCase.class);
    cancelUseCase = mock(CancelOrderUseCase.class);
    authorizationService = new AuthorizationService();
    mapper = mock(OrderMapper.class);
    orderItemRepository = mock(OrderItemRepository.class);

    OrderSaveUseCase saveUseCase = mock(OrderSaveUseCase.class);
    OrderUpdateUseCase updateUseCase = mock(OrderUpdateUseCase.class);
    OrderDeleteUseCase deleteUseCase = mock(OrderDeleteUseCase.class);
    OrderFindAllUseCase findAllUseCase = mock(OrderFindAllUseCase.class);
    FindOrdersByCustomerIdUseCase findByCustomerIdUseCase =
        mock(FindOrdersByCustomerIdUseCase.class);
    FindOrdersBySellerUseCase findBySellerUseCase = mock(FindOrdersBySellerUseCase.class);
    ConfirmDeliveryUseCase confirmDeliveryUseCase = mock(ConfirmDeliveryUseCase.class);
    PrepareOrderUseCase prepareOrderUseCase = mock(PrepareOrderUseCase.class);
    ShipOrderUseCase shipOrderUseCase = mock(ShipOrderUseCase.class);
    OutForDeliveryUseCase outForDeliveryUseCase = mock(OutForDeliveryUseCase.class);
    UpdateTrackingUseCase updateTrackingUseCase = mock(UpdateTrackingUseCase.class);
    CompleteOrderUseCase completeOrderUseCase = mock(CompleteOrderUseCase.class);
    DisputeOrderUseCase disputeOrderUseCase = mock(DisputeOrderUseCase.class);
    RefundOrderUseCase refundOrderUseCase = mock(RefundOrderUseCase.class);
    OrderSummaryUseCase orderSummaryUseCase = mock(OrderSummaryUseCase.class);
    AddTrackingEventUseCase addTrackingEventUseCase = mock(AddTrackingEventUseCase.class);
    GetTrackingEventsUseCase getTrackingEventsUseCase = mock(GetTrackingEventsUseCase.class);
    SearchOrdersUseCase searchOrdersUseCase = mock(SearchOrdersUseCase.class);
    OrderStatusCountsUseCase orderStatusCountsUseCase = mock(OrderStatusCountsUseCase.class);

    OrderAuthorizationHelper authHelper =
        new OrderAuthorizationHelper(findByIdUseCase, orderItemRepository, authorizationService);

    OrderManagementController managementController =
        new OrderManagementController(
            saveUseCase,
            updateUseCase,
            deleteUseCase,
            findByIdUseCase,
            findByCustomerIdUseCase,
            mapper,
            authHelper,
            mock(com.eliteshop.colombia.shared.domain.LocationValidationService.class));

    OrderStatusController statusController =
        new OrderStatusController(
            cancelUseCase,
            confirmDeliveryUseCase,
            prepareOrderUseCase,
            shipOrderUseCase,
            outForDeliveryUseCase,
            completeOrderUseCase,
            disputeOrderUseCase,
            refundOrderUseCase,
            updateTrackingUseCase,
            addTrackingEventUseCase,
            getTrackingEventsUseCase,
            authHelper);

    OrderSellerController sellerController =
        new OrderSellerController(
            findBySellerUseCase,
            orderSummaryUseCase,
            searchOrdersUseCase,
            orderStatusCountsUseCase,
            mapper,
            authHelper);

    mockMvc =
        MockMvcBuilders.standaloneSetup(managementController, statusController, sellerController)
            .build();
  }

  @Test
  void shouldAllowWhenReadingOwnOrder() throws Exception {
    Order order = buildOrder(customerId);
    when(findByIdUseCase.execute(any())).thenReturn(Optional.of(order));
    when(mapper.toResponseWithItems(any())).thenReturn(new OrderResponse());

    mockMvc
        .perform(get("/api/v1/orders/" + orderId).principal(authentication(customerId)))
        .andExpect(status().isOk());
  }

  @Test
  void shouldDenyWhenReadingOtherOrder() throws Exception {
    Order order = buildOrder(otherCustomerId);
    when(findByIdUseCase.execute(any())).thenReturn(Optional.of(order));

    mockMvc
        .perform(get("/api/v1/orders/" + orderId).principal(authentication(customerId)))
        .andExpect(status().isForbidden());
  }

  @Test
  void shouldDenyWhenCancellingOtherOrder() throws Exception {
    Order order = buildOrder(otherCustomerId);
    when(findByIdUseCase.execute(any())).thenReturn(Optional.of(order));

    mockMvc
        .perform(
            patch("/api/v1/orders/" + orderId + "/cancel").principal(authentication(customerId)))
        .andExpect(status().isForbidden());
  }

  @Test
  void shouldAllowWhenSellerHasItemsInOrder() throws Exception {
    Order order = buildOrder(customerId);
    when(findByIdUseCase.execute(any())).thenReturn(Optional.of(order));
    when(mapper.toResponseWithItems(any())).thenReturn(new OrderResponse());

    OrderItem item = mock(OrderItem.class);
    when(item.getSellerId())
        .thenReturn(new com.eliteshop.colombia.order.domain.model.OrderItemSellerId(sellerId));
    when(orderItemRepository.findByOrderId(orderId)).thenReturn(List.of(item));

    mockMvc
        .perform(get("/api/v1/orders/" + orderId).principal(authentication(customerId)))
        .andExpect(status().isOk());
  }

  private Order buildOrder(UUID orderCustomerId) {
    return new Order(
        new OrderId(orderId),
        new OrderCustomerId(orderCustomerId),
        OrderStatus.PAID,
        new OrderTotalAmount(new java.math.BigDecimal("100000")),
        new OrderShippingAddress("Calle 123"),
        new OrderShippingDepartment("Bogota"),
        new OrderShippingCity("Bogota"),
        new OrderCreatedAt(new Timestamp(System.currentTimeMillis())),
        null,
        null,
        null,
        null,
        null);
  }

  private Authentication authentication(UUID userId) {
    return new UsernamePasswordAuthenticationToken(
        userId.toString(), null, List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
  }
}
