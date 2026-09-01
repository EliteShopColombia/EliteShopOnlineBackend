package com.eliteshop.colombia.order.infrastructure.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.eliteshop.colombia.order.application.*;
import com.eliteshop.colombia.order.domain.exception.InvalidOrderStatusTransitionException;
import com.eliteshop.colombia.order.domain.exception.OrderAlreadyExistsException;
import com.eliteshop.colombia.order.domain.model.*;
import com.eliteshop.colombia.order.domain.model.tracking.TrackingEvent;
import com.eliteshop.colombia.order.domain.repository.OrderItemRepository;
import com.eliteshop.colombia.order.infrastructure.controller.dto.OrderRequest;
import com.eliteshop.colombia.order.infrastructure.controller.dto.OrderStatusCountResponse;
import com.eliteshop.colombia.order.infrastructure.controller.dto.OrderSummaryResponse;
import com.eliteshop.colombia.order.infrastructure.controller.dto.TrackingEventRequest;
import com.eliteshop.colombia.order.infrastructure.controller.dto.TrackingInfoRequest;
import com.eliteshop.colombia.order.infrastructure.mapper.OrderMapper;
import com.eliteshop.colombia.shared.domain.PageResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class OrderControllerTest {

  private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private OrderSaveUseCase saveUseCase;
  private OrderUpdateUseCase updateUseCase;
  private OrderDeleteUseCase deleteUseCase;
  private OrderFindAllUseCase findAllUseCase;
  private OrderFindByIdUseCase findByIdUseCase;
  private FindOrdersByCustomerIdUseCase findByCustomerIdUseCase;
  private FindOrdersBySellerUseCase findBySellerUseCase;
  private CancelOrderUseCase cancelOrderUseCase;
  private ConfirmDeliveryUseCase confirmDeliveryUseCase;
  private PrepareOrderUseCase prepareOrderUseCase;
  private ShipOrderUseCase shipOrderUseCase;
  private OutForDeliveryUseCase outForDeliveryUseCase;
  private UpdateTrackingUseCase updateTrackingUseCase;
  private CompleteOrderUseCase completeOrderUseCase;
  private DisputeOrderUseCase disputeOrderUseCase;
  private RefundOrderUseCase refundOrderUseCase;
  private OrderSummaryUseCase orderSummaryUseCase;
  private AddTrackingEventUseCase addTrackingEventUseCase;
  private GetTrackingEventsUseCase getTrackingEventsUseCase;
  private SearchOrdersUseCase searchOrdersUseCase;
  private OrderStatusCountsUseCase orderStatusCountsUseCase;
  private OrderMapper mapper;

  @BeforeEach
  void setUp() {
    saveUseCase = mock(OrderSaveUseCase.class);
    updateUseCase = mock(OrderUpdateUseCase.class);
    deleteUseCase = mock(OrderDeleteUseCase.class);
    findAllUseCase = mock(OrderFindAllUseCase.class);
    findByIdUseCase = mock(OrderFindByIdUseCase.class);
    findByCustomerIdUseCase = mock(FindOrdersByCustomerIdUseCase.class);
    findBySellerUseCase = mock(FindOrdersBySellerUseCase.class);
    cancelOrderUseCase = mock(CancelOrderUseCase.class);
    confirmDeliveryUseCase = mock(ConfirmDeliveryUseCase.class);
    prepareOrderUseCase = mock(PrepareOrderUseCase.class);
    shipOrderUseCase = mock(ShipOrderUseCase.class);
    outForDeliveryUseCase = mock(OutForDeliveryUseCase.class);
    updateTrackingUseCase = mock(UpdateTrackingUseCase.class);
    completeOrderUseCase = mock(CompleteOrderUseCase.class);
    disputeOrderUseCase = mock(DisputeOrderUseCase.class);
    refundOrderUseCase = mock(RefundOrderUseCase.class);
    orderSummaryUseCase = mock(OrderSummaryUseCase.class);
    addTrackingEventUseCase = mock(AddTrackingEventUseCase.class);
    getTrackingEventsUseCase = mock(GetTrackingEventsUseCase.class);
    searchOrdersUseCase = mock(SearchOrdersUseCase.class);
    orderStatusCountsUseCase = mock(OrderStatusCountsUseCase.class);
    OrderItemRepository orderItemRepository = mock(OrderItemRepository.class);
    mapper = new OrderMapper(orderItemRepository);

    OrderAuthorizationHelper authHelper =
        new OrderAuthorizationHelper(findByIdUseCase, orderItemRepository, null);

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
            cancelOrderUseCase,
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
  void shouldCreateOrder() throws Exception {
    UUID customerId = UUID.randomUUID();

    OrderRequest request = new OrderRequest();
    request.setCustomerId(customerId);
    request.setTotalAmount(new BigDecimal("250000"));
    request.setShippingAddress("Calle 100 #15-20");
    request.setShippingDepartment("Bogota");
    request.setShippingCity("Bogota D.C.");

    mockMvc
        .perform(
            post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(authentication(customerId)))
        .andExpect(status().isCreated());
  }

  @Test
  void shouldRejectRequestWithoutRequiredFields() throws Exception {
    OrderRequest request = new OrderRequest();

    mockMvc
        .perform(
            post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(authentication(UUID.randomUUID())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldRejectRequestWithoutCustomerId() throws Exception {
    OrderRequest request = new OrderRequest();
    request.setTotalAmount(new BigDecimal("250000"));
    request.setShippingAddress("Calle 100 #15-20");
    request.setShippingDepartment("Bogota");
    request.setShippingCity("Bogota D.C.");

    mockMvc
        .perform(
            post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(authentication(UUID.randomUUID())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldRejectRequestWithoutTotalAmount() throws Exception {
    UUID customerId = UUID.randomUUID();
    OrderRequest request = new OrderRequest();
    request.setCustomerId(customerId);
    request.setShippingAddress("Calle 100 #15-20");
    request.setShippingDepartment("Bogota");
    request.setShippingCity("Bogota D.C.");

    mockMvc
        .perform(
            post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(authentication(customerId)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn409WhenOrderAlreadyExists() throws Exception {
    UUID customerId = UUID.randomUUID();
    doThrow(new OrderAlreadyExistsException("Order already exists"))
        .when(saveUseCase)
        .execute(any());

    OrderRequest request = new OrderRequest();
    request.setCustomerId(customerId);
    request.setTotalAmount(new BigDecimal("250000"));
    request.setShippingAddress("Calle 100 #15-20");
    request.setShippingDepartment("Bogota");
    request.setShippingCity("Bogota D.C.");

    mockMvc
        .perform(
            post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(authentication(customerId)))
        .andExpect(status().isConflict());
  }

  @Test
  void shouldFindOrderById() throws Exception {
    UUID customerId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    Order order = buildOrder(orderId, customerId);
    when(findByIdUseCase.execute(any(OrderId.class))).thenReturn(Optional.of(order));

    mockMvc
        .perform(get("/api/v1/orders/{id}", orderId).principal(authentication(customerId)))
        .andExpect(status().isOk());
  }

  @Test
  void shouldReturn404WhenOrderNotFound() throws Exception {
    UUID orderId = UUID.randomUUID();
    when(findByIdUseCase.execute(any(OrderId.class))).thenReturn(Optional.empty());

    mockMvc
        .perform(get("/api/v1/orders/{id}", orderId).principal(authentication(UUID.randomUUID())))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldFindAllOrders() throws Exception {
    UUID customerId = UUID.randomUUID();
    PageResult<Order> pageResult = PageResult.of(List.of(), 0, 25, 0);
    when(findByCustomerIdUseCase.execute(any(OrderCustomerId.class), eq(0), eq(25)))
        .thenReturn(pageResult);

    mockMvc
        .perform(get("/api/v1/orders").principal(authentication(customerId)))
        .andExpect(status().isOk());
  }

  @Test
  void shouldReturnEmptyListWhenNoOrders() throws Exception {
    UUID customerId = UUID.randomUUID();
    PageResult<Order> emptyResult = PageResult.of(List.of(), 0, 25, 0);
    when(findByCustomerIdUseCase.execute(any(OrderCustomerId.class), eq(0), eq(25)))
        .thenReturn(emptyResult);

    mockMvc
        .perform(get("/api/v1/orders").principal(authentication(customerId)))
        .andExpect(status().isOk());
  }

  @Test
  void shouldFindOrdersByCustomerId() throws Exception {
    UUID customerId = UUID.randomUUID();
    PageResult<Order> pageResult = PageResult.of(List.of(), 0, 25, 0);
    when(findByCustomerIdUseCase.execute(any(OrderCustomerId.class), eq(0), eq(25)))
        .thenReturn(pageResult);

    mockMvc
        .perform(
            get("/api/v1/orders/customer/{customerId}", customerId)
                .principal(authentication(customerId)))
        .andExpect(status().isOk());
  }

  @Test
  void shouldReturnEmptyListWhenNoOrdersForCustomer() throws Exception {
    UUID customerId = UUID.randomUUID();
    PageResult<Order> emptyResult = PageResult.of(List.of(), 0, 25, 0);
    when(findByCustomerIdUseCase.execute(any(OrderCustomerId.class), eq(0), eq(25)))
        .thenReturn(emptyResult);

    mockMvc
        .perform(
            get("/api/v1/orders/customer/{customerId}", customerId)
                .principal(authentication(customerId)))
        .andExpect(status().isOk());
  }

  @Test
  void shouldCancelOrder() throws Exception {
    UUID customerId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    Order order = buildOrder(orderId, customerId);
    when(findByIdUseCase.execute(any(OrderId.class))).thenReturn(Optional.of(order));

    mockMvc
        .perform(patch("/api/v1/orders/{id}/cancel", orderId).principal(authentication(customerId)))
        .andExpect(status().isNoContent());
  }

  @Test
  void shouldReturn400WhenCancellingInvalidStatus() throws Exception {
    UUID customerId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    Order order = buildOrder(orderId, customerId);
    when(findByIdUseCase.execute(any(OrderId.class))).thenReturn(Optional.of(order));
    doThrow(new InvalidOrderStatusTransitionException("Cannot cancel"))
        .when(cancelOrderUseCase)
        .execute(any());

    mockMvc
        .perform(patch("/api/v1/orders/{id}/cancel", orderId).principal(authentication(customerId)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldConfirmDelivery() throws Exception {
    UUID customerId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    Order order = buildOrder(orderId, customerId);
    when(findByIdUseCase.execute(any(OrderId.class))).thenReturn(Optional.of(order));

    mockMvc
        .perform(
            patch("/api/v1/orders/{id}/confirm-delivery", orderId)
                .principal(authentication(customerId)))
        .andExpect(status().isNoContent());
  }

  @Test
  void shouldReturn400WhenConfirmingDeliveryForInvalidStatus() throws Exception {
    UUID customerId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    Order order = buildOrder(orderId, customerId);
    when(findByIdUseCase.execute(any(OrderId.class))).thenReturn(Optional.of(order));
    doThrow(new InvalidOrderStatusTransitionException("Cannot confirm"))
        .when(confirmDeliveryUseCase)
        .execute(any());

    mockMvc
        .perform(
            patch("/api/v1/orders/{id}/confirm-delivery", orderId)
                .principal(authentication(customerId)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldPrepareOrder() throws Exception {
    UUID customerId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    Order order = buildOrder(orderId, customerId);
    when(findByIdUseCase.execute(any(OrderId.class))).thenReturn(Optional.of(order));

    mockMvc
        .perform(
            patch("/api/v1/orders/{id}/prepare", orderId).principal(authentication(customerId)))
        .andExpect(status().isNoContent());
  }

  @Test
  void shouldReturn400WhenPreparingInvalidStatus() throws Exception {
    UUID customerId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    Order order = buildOrder(orderId, customerId);
    when(findByIdUseCase.execute(any(OrderId.class))).thenReturn(Optional.of(order));
    doThrow(new InvalidOrderStatusTransitionException("Cannot prepare"))
        .when(prepareOrderUseCase)
        .execute(any());

    mockMvc
        .perform(
            patch("/api/v1/orders/{id}/prepare", orderId).principal(authentication(customerId)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldShipOrder() throws Exception {
    UUID customerId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    Order order = buildOrder(orderId, customerId);
    when(findByIdUseCase.execute(any(OrderId.class))).thenReturn(Optional.of(order));

    mockMvc
        .perform(patch("/api/v1/orders/{id}/ship", orderId).principal(authentication(customerId)))
        .andExpect(status().isNoContent());
  }

  @Test
  void shouldReturn400WhenShippingInvalidStatus() throws Exception {
    UUID customerId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    Order order = buildOrder(orderId, customerId);
    when(findByIdUseCase.execute(any(OrderId.class))).thenReturn(Optional.of(order));
    doThrow(new InvalidOrderStatusTransitionException("Cannot ship"))
        .when(shipOrderUseCase)
        .execute(any());

    mockMvc
        .perform(patch("/api/v1/orders/{id}/ship", orderId).principal(authentication(customerId)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldMarkOrderOutForDelivery() throws Exception {
    UUID customerId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    Order order = buildOrder(orderId, customerId);
    when(findByIdUseCase.execute(any(OrderId.class))).thenReturn(Optional.of(order));

    mockMvc
        .perform(
            patch("/api/v1/orders/{id}/out-for-delivery", orderId)
                .principal(authentication(customerId)))
        .andExpect(status().isNoContent());
  }

  @Test
  void shouldReturn400WhenMarkingOutForDeliveryInvalidStatus() throws Exception {
    UUID customerId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    Order order = buildOrder(orderId, customerId);
    when(findByIdUseCase.execute(any(OrderId.class))).thenReturn(Optional.of(order));
    doThrow(new InvalidOrderStatusTransitionException("Cannot out for delivery"))
        .when(outForDeliveryUseCase)
        .execute(any());

    mockMvc
        .perform(
            patch("/api/v1/orders/{id}/out-for-delivery", orderId)
                .principal(authentication(customerId)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldCompleteOrder() throws Exception {
    UUID customerId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    Order order = buildOrder(orderId, customerId);
    when(findByIdUseCase.execute(any(OrderId.class))).thenReturn(Optional.of(order));

    mockMvc
        .perform(
            patch("/api/v1/orders/{id}/complete", orderId).principal(authentication(customerId)))
        .andExpect(status().isNoContent());
  }

  @Test
  void shouldReturn400WhenCompletingInvalidStatus() throws Exception {
    UUID customerId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    Order order = buildOrder(orderId, customerId);
    when(findByIdUseCase.execute(any(OrderId.class))).thenReturn(Optional.of(order));
    doThrow(new InvalidOrderStatusTransitionException("Cannot complete"))
        .when(completeOrderUseCase)
        .execute(any());

    mockMvc
        .perform(
            patch("/api/v1/orders/{id}/complete", orderId).principal(authentication(customerId)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldDisputeOrder() throws Exception {
    UUID customerId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    Order order = buildOrder(orderId, customerId);
    when(findByIdUseCase.execute(any(OrderId.class))).thenReturn(Optional.of(order));

    mockMvc
        .perform(
            patch("/api/v1/orders/{id}/dispute", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\": \"PRODUCT_DAMAGED\"}")
                .principal(authentication(customerId)))
        .andExpect(status().isNoContent());
  }

  @Test
  void shouldReturn400WhenDisputingInvalidStatus() throws Exception {
    UUID customerId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    Order order = buildOrder(orderId, customerId);
    when(findByIdUseCase.execute(any(OrderId.class))).thenReturn(Optional.of(order));
    doThrow(new InvalidOrderStatusTransitionException("Cannot dispute"))
        .when(disputeOrderUseCase)
        .execute(any(), any(), any());

    mockMvc
        .perform(
            patch("/api/v1/orders/{id}/dispute", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\": \"PRODUCT_DAMAGED\"}")
                .principal(authentication(customerId)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldRefundOrder() throws Exception {
    UUID customerId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    Order order = buildOrder(orderId, customerId);
    when(findByIdUseCase.execute(any(OrderId.class))).thenReturn(Optional.of(order));

    mockMvc
        .perform(patch("/api/v1/orders/{id}/refund", orderId).principal(authentication(customerId)))
        .andExpect(status().isNoContent());
  }

  @Test
  void shouldReturn400WhenRefundingInvalidStatus() throws Exception {
    UUID customerId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    Order order = buildOrder(orderId, customerId);
    when(findByIdUseCase.execute(any(OrderId.class))).thenReturn(Optional.of(order));
    doThrow(new InvalidOrderStatusTransitionException("Cannot refund"))
        .when(refundOrderUseCase)
        .execute(any(), any());

    mockMvc
        .perform(patch("/api/v1/orders/{id}/refund", orderId).principal(authentication(customerId)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldDeleteOrder() throws Exception {
    UUID customerId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    Order order = buildOrder(orderId, customerId);
    when(findByIdUseCase.execute(any(OrderId.class))).thenReturn(Optional.of(order));

    mockMvc
        .perform(delete("/api/v1/orders/{id}", orderId).principal(authentication(customerId)))
        .andExpect(status().isNoContent());
  }

  @Test
  void shouldFindOrdersBySeller() throws Exception {
    UUID sellerId = UUID.randomUUID();
    PageResult<Order> pageResult = PageResult.of(List.of(), 0, 25, 0);
    when(findBySellerUseCase.execute(eq(sellerId), eq(0), eq(25))).thenReturn(pageResult);

    mockMvc
        .perform(
            get("/api/v1/orders/seller/{sellerId}", sellerId).principal(authentication(sellerId)))
        .andExpect(status().isOk());
  }

  @Test
  void shouldReturnEmptyListWhenNoOrdersForSeller() throws Exception {
    UUID sellerId = UUID.randomUUID();
    PageResult<Order> emptyResult = PageResult.of(List.of(), 0, 25, 0);
    when(findBySellerUseCase.execute(eq(sellerId), eq(0), eq(25))).thenReturn(emptyResult);

    mockMvc
        .perform(
            get("/api/v1/orders/seller/{sellerId}", sellerId).principal(authentication(sellerId)))
        .andExpect(status().isOk());
  }

  @Test
  void shouldGetSellerSummary() throws Exception {
    UUID sellerId = UUID.randomUUID();
    OrderSummaryResponse summary =
        new OrderSummaryResponse(
            sellerId,
            10,
            new BigDecimal("500000"),
            java.util.Map.of("PAID", 5, "SHIPPED", 3, "DELIVERED", 2),
            new BigDecimal("50000"));
    when(orderSummaryUseCase.execute(sellerId)).thenReturn(summary);

    mockMvc
        .perform(
            get("/api/v1/orders/seller/{sellerId}/summary", sellerId)
                .principal(authentication(sellerId)))
        .andExpect(status().isOk());
  }

  @Test
  void shouldSearchOrders() throws Exception {
    UUID sellerId = UUID.randomUUID();
    SearchOrdersUseCase.SearchResult result =
        new SearchOrdersUseCase.SearchResult(List.of(), 0, 0, 10, 0);
    when(searchOrdersUseCase.execute(eq(sellerId), any(), eq(0), eq(10))).thenReturn(result);

    mockMvc
        .perform(
            get("/api/v1/orders/seller/{sellerId}/search", sellerId)
                .principal(authentication(sellerId)))
        .andExpect(status().isOk());
  }

  @Test
  void shouldSearchOrdersWithoutStatusFilter() throws Exception {
    UUID sellerId = UUID.randomUUID();
    SearchOrdersUseCase.SearchResult result =
        new SearchOrdersUseCase.SearchResult(List.of(), 0, 0, 10, 0);
    when(searchOrdersUseCase.execute(eq(sellerId), any(), eq(0), eq(10))).thenReturn(result);

    mockMvc
        .perform(
            get("/api/v1/orders/seller/{sellerId}/search", sellerId)
                .principal(authentication(sellerId)))
        .andExpect(status().isOk());
  }

  @Test
  void shouldGetStatusCounts() throws Exception {
    UUID sellerId = UUID.randomUUID();
    OrderStatusCountResponse counts =
        new OrderStatusCountResponse(sellerId, 10, Map.of("PAID", 5, "SHIPPED", 3, "DELIVERED", 2));
    when(orderStatusCountsUseCase.execute(sellerId)).thenReturn(counts);

    mockMvc
        .perform(
            get("/api/v1/orders/seller/{sellerId}/status-counts", sellerId)
                .principal(authentication(sellerId)))
        .andExpect(status().isOk());
  }

  @Test
  void shouldGetTrackingEvents() throws Exception {
    UUID customerId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    Order order = buildOrder(orderId, customerId);
    when(findByIdUseCase.execute(any(OrderId.class))).thenReturn(Optional.of(order));
    when(getTrackingEventsUseCase.execute(any(OrderId.class))).thenReturn(List.of());

    mockMvc
        .perform(get("/api/v1/orders/{id}/tracking", orderId).principal(authentication(customerId)))
        .andExpect(status().isOk());
  }

  @Test
  void shouldAddTrackingEvent() throws Exception {
    UUID customerId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    Order order = buildOrder(orderId, customerId);
    when(findByIdUseCase.execute(any(OrderId.class))).thenReturn(Optional.of(order));

    TrackingEvent event =
        new TrackingEvent(
            new com.eliteshop.colombia.order.domain.model.tracking.TrackingEventId(
                UUID.randomUUID()),
            new com.eliteshop.colombia.order.domain.model.tracking.TrackingEventOrderId(orderId),
            com.eliteshop.colombia.order.domain.model.tracking.TrackingEventStatus.RECEIVED,
            "Bogota",
            "Paquete enviado",
            Timestamp.from(Instant.now()),
            Timestamp.from(Instant.now()));
    when(addTrackingEventUseCase.execute(any(OrderId.class), any(), any(), any(), any()))
        .thenReturn(event);

    TrackingEventRequest request = new TrackingEventRequest();
    request.setStatus("SHIPPED");
    request.setLocation("Bogota");
    request.setDescription("Paquete enviado");
    request.setEventTimestamp(Timestamp.from(Instant.now()));

    mockMvc
        .perform(
            post("/api/v1/orders/{id}/tracking", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(authentication(customerId)))
        .andExpect(status().isCreated());
  }

  @Test
  void shouldRejectTrackingEventRequestWithoutStatus() throws Exception {
    UUID customerId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    Order order = buildOrder(orderId, customerId);
    when(findByIdUseCase.execute(any(OrderId.class))).thenReturn(Optional.of(order));

    TrackingEventRequest request = new TrackingEventRequest();
    request.setLocation("Bogota");

    mockMvc
        .perform(
            post("/api/v1/orders/{id}/tracking", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(authentication(customerId)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldRejectTrackingRequestWithoutTrackingNumber() throws Exception {
    UUID customerId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    Order order = buildOrder(orderId, customerId);
    when(findByIdUseCase.execute(any(OrderId.class))).thenReturn(Optional.of(order));

    TrackingInfoRequest request = new TrackingInfoRequest();
    request.setShippingCarrier("Servientrega");

    mockMvc
        .perform(
            patch("/api/v1/orders/{id}/tracking", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(authentication(customerId)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldRejectTrackingRequestWithoutCarrier() throws Exception {
    UUID customerId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    Order order = buildOrder(orderId, customerId);
    when(findByIdUseCase.execute(any(OrderId.class))).thenReturn(Optional.of(order));

    TrackingInfoRequest request = new TrackingInfoRequest();
    request.setTrackingNumber("TRK-123");

    mockMvc
        .perform(
            patch("/api/v1/orders/{id}/tracking", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(authentication(customerId)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldUpdateTracking() throws Exception {
    UUID customerId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    Order order = buildOrder(orderId, customerId);
    when(findByIdUseCase.execute(any(OrderId.class))).thenReturn(Optional.of(order));

    TrackingInfoRequest request = new TrackingInfoRequest();
    request.setTrackingNumber("TRK-123");
    request.setShippingCarrier("Servientrega");
    request.setShippingLabelUrl("https://label.example.com/label.pdf");

    mockMvc
        .perform(
            patch("/api/v1/orders/{id}/tracking", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(authentication(customerId)))
        .andExpect(status().isNoContent());
  }

  private Order buildOrder(UUID orderId, UUID customerId) {
    return new Order(
        new OrderId(orderId),
        new OrderCustomerId(customerId),
        OrderStatus.PAID,
        new OrderTotalAmount(new BigDecimal("250000")),
        new OrderShippingAddress("Calle 100 #15-20"),
        new OrderShippingDepartment("Bogota"),
        new OrderShippingCity("Bogota D.C."),
        new OrderCreatedAt(Timestamp.from(Instant.now())),
        null,
        null,
        null,
        null,
        null);
  }

  private UsernamePasswordAuthenticationToken authentication(UUID userId) {
    return new UsernamePasswordAuthenticationToken(
        userId.toString(),
        null,
        java.util.List.of(
            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                "ROLE_CUSTOMER")));
  }
}
