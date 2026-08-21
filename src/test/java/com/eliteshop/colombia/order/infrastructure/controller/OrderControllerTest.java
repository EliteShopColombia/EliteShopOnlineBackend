package com.eliteshop.colombia.order.infrastructure.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.eliteshop.colombia.order.application.*;
import com.eliteshop.colombia.order.domain.exception.InvalidOrderStatusTransitionException;
import com.eliteshop.colombia.order.domain.exception.OrderAlreadyExistsException;
import com.eliteshop.colombia.order.domain.exception.OrderNotFoundException;
import com.eliteshop.colombia.order.domain.model.*;
import com.eliteshop.colombia.order.domain.model.tracking.TrackingEvent;
import com.eliteshop.colombia.order.domain.repository.OrderItemRepository;
import com.eliteshop.colombia.order.infrastructure.controller.dto.OrderRequest;
import com.eliteshop.colombia.order.infrastructure.controller.dto.OrderStatusCountResponse;
import com.eliteshop.colombia.order.infrastructure.controller.dto.OrderSummaryResponse;
import com.eliteshop.colombia.order.infrastructure.controller.dto.TrackingEventRequest;
import com.eliteshop.colombia.order.infrastructure.controller.dto.TrackingInfoRequest;
import com.eliteshop.colombia.order.infrastructure.mapper.OrderMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
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

    OrderController controller =
        new OrderController(
            saveUseCase,
            updateUseCase,
            deleteUseCase,
            findAllUseCase,
            findByIdUseCase,
            findByCustomerIdUseCase,
            findBySellerUseCase,
            cancelOrderUseCase,
            confirmDeliveryUseCase,
            prepareOrderUseCase,
            shipOrderUseCase,
            outForDeliveryUseCase,
            updateTrackingUseCase,
            completeOrderUseCase,
            disputeOrderUseCase,
            refundOrderUseCase,
            orderSummaryUseCase,
            addTrackingEventUseCase,
            getTrackingEventsUseCase,
            searchOrdersUseCase,
            orderStatusCountsUseCase,
            mapper);
    mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
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
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.customerId").value(customerId.toString()))
        .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
        .andExpect(jsonPath("$.totalAmount").value(250000))
        .andExpect(jsonPath("$.shippingAddress").value("Calle 100 #15-20"))
        .andExpect(jsonPath("$.shippingDepartment").value("Bogota"))
        .andExpect(jsonPath("$.shippingCity").value("Bogota D.C."));

    verify(saveUseCase).execute(any(Order.class));
  }

  @Test
  void shouldRejectRequestWithoutRequiredFields() throws Exception {
    OrderRequest request = new OrderRequest();

    mockMvc
        .perform(
            post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldRejectRequestWithoutCustomerId() throws Exception {
    OrderRequest request = new OrderRequest();
    request.setTotalAmount(new BigDecimal("100000"));
    request.setShippingAddress("Calle 50");
    request.setShippingDepartment("Bogota");
    request.setShippingCity("Bogota D.C.");

    mockMvc
        .perform(
            post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldRejectRequestWithoutTotalAmount() throws Exception {
    OrderRequest request = new OrderRequest();
    request.setCustomerId(UUID.randomUUID());
    request.setShippingAddress("Calle 50");
    request.setShippingDepartment("Bogota");
    request.setShippingCity("Bogota D.C.");

    mockMvc
        .perform(
            post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn409WhenOrderAlreadyExists() throws Exception {
    UUID customerId = UUID.randomUUID();

    OrderRequest request = new OrderRequest();
    request.setCustomerId(customerId);
    request.setTotalAmount(new BigDecimal("150000"));
    request.setShippingAddress("Carrera 7 #32-16");
    request.setShippingDepartment("Antioquia");
    request.setShippingCity("Medellin");

    doThrow(new OrderAlreadyExistsException("This order already exist in the platform"))
        .when(saveUseCase)
        .execute(any(Order.class));

    mockMvc
        .perform(
            post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict());
  }

  @Test
  void shouldFindAllOrders() throws Exception {
    UUID orderId1 = UUID.randomUUID();
    UUID orderId2 = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    List<Order> orders =
        List.of(
            buildOrder(orderId1, customerId, OrderStatus.PENDING_PAYMENT, new BigDecimal("100000")),
            buildOrder(orderId2, customerId, OrderStatus.PAID, new BigDecimal("200000")));

    when(findAllUseCase.execute()).thenReturn(orders);

    mockMvc
        .perform(get("/api/v1/orders"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].customerId").value(customerId.toString()))
        .andExpect(jsonPath("$[0].status").value("PENDING_PAYMENT"))
        .andExpect(jsonPath("$[1].status").value("PAID"));
  }

  @Test
  void shouldReturnEmptyListWhenNoOrders() throws Exception {
    when(findAllUseCase.execute()).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/api/v1/orders"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  void shouldFindOrderById() throws Exception {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    Order order =
        buildOrder(orderId, customerId, OrderStatus.PENDING_PAYMENT, new BigDecimal("300000"));

    when(findByIdUseCase.execute(any(OrderId.class))).thenReturn(Optional.of(order));

    mockMvc
        .perform(get("/api/v1/orders/{id}", orderId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(orderId.toString()))
        .andExpect(jsonPath("$.customerId").value(customerId.toString()))
        .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
        .andExpect(jsonPath("$.totalAmount").value(300000));
  }

  @Test
  void shouldReturn404WhenOrderNotFound() throws Exception {
    UUID orderId = UUID.randomUUID();

    when(findByIdUseCase.execute(any(OrderId.class))).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/v1/orders/{id}", orderId)).andExpect(status().isNotFound());
  }

  @Test
  void shouldUpdateOrder() throws Exception {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    OrderRequest request = new OrderRequest();
    request.setCustomerId(customerId);
    request.setTotalAmount(new BigDecimal("400000"));
    request.setShippingAddress("Calle 85 #11-50");
    request.setShippingDepartment("Bogota");
    request.setShippingCity("Bogota D.C.");
    request.setStatus("PAID");

    Order updatedOrder =
        buildOrder(orderId, customerId, OrderStatus.PAID, new BigDecimal("400000"));

    when(findByIdUseCase.execute(any(OrderId.class))).thenReturn(Optional.of(updatedOrder));

    mockMvc
        .perform(
            put("/api/v1/orders/{id}", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(orderId.toString()))
        .andExpect(jsonPath("$.status").value("PAID"))
        .andExpect(jsonPath("$.totalAmount").value(400000));

    verify(updateUseCase).execute(any(Order.class));
  }

  @Test
  void shouldReturn404WhenUpdatingNonExistentOrder() throws Exception {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    OrderRequest request = new OrderRequest();
    request.setCustomerId(customerId);
    request.setTotalAmount(new BigDecimal("200000"));
    request.setShippingAddress("Carrera 15 #80-50");
    request.setShippingDepartment("Bogota");
    request.setShippingCity("Usaquen");
    request.setStatus("SHIPPED");

    doThrow(new OrderNotFoundException("The order not exist in our platform"))
        .when(updateUseCase)
        .execute(any(Order.class));

    mockMvc
        .perform(
            put("/api/v1/orders/{id}", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldDeleteOrder() throws Exception {
    UUID orderId = UUID.randomUUID();

    mockMvc.perform(delete("/api/v1/orders/{id}", orderId)).andExpect(status().isNoContent());

    verify(deleteUseCase).execute(any(OrderId.class));
  }

  @Test
  void shouldReturn404WhenDeletingNonExistentOrder() throws Exception {
    UUID orderId = UUID.randomUUID();

    doThrow(new OrderNotFoundException("The order not exist in our platform"))
        .when(deleteUseCase)
        .execute(any(OrderId.class));

    mockMvc.perform(delete("/api/v1/orders/{id}", orderId)).andExpect(status().isNotFound());
  }

  @Test
  void shouldFindOrdersByCustomerId() throws Exception {
    UUID customerId = UUID.randomUUID();
    UUID orderId1 = UUID.randomUUID();
    UUID orderId2 = UUID.randomUUID();

    List<Order> orders =
        List.of(
            buildOrder(orderId1, customerId, OrderStatus.PAID, new BigDecimal("150000")),
            buildOrder(orderId2, customerId, OrderStatus.SHIPPED, new BigDecimal("250000")));

    when(findByCustomerIdUseCase.execute(any(OrderCustomerId.class))).thenReturn(orders);

    mockMvc
        .perform(get("/api/v1/orders/customer/{customerId}", customerId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].customerId").value(customerId.toString()))
        .andExpect(jsonPath("$[0].status").value("PAID"))
        .andExpect(jsonPath("$[1].status").value("SHIPPED"));
  }

  @Test
  void shouldReturnEmptyListWhenNoOrdersForCustomer() throws Exception {
    UUID customerId = UUID.randomUUID();

    when(findByCustomerIdUseCase.execute(any(OrderCustomerId.class)))
        .thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/api/v1/orders/customer/{customerId}", customerId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  void shouldCancelOrder() throws Exception {
    UUID orderId = UUID.randomUUID();

    mockMvc.perform(patch("/api/v1/orders/{id}/cancel", orderId)).andExpect(status().isNoContent());

    verify(cancelOrderUseCase).execute(any(OrderId.class));
  }

  @Test
  void shouldReturn404WhenCancellingNonExistentOrder() throws Exception {
    UUID orderId = UUID.randomUUID();

    doThrow(new OrderNotFoundException("The order not exist in our platform"))
        .when(cancelOrderUseCase)
        .execute(any(OrderId.class));

    mockMvc.perform(patch("/api/v1/orders/{id}/cancel", orderId)).andExpect(status().isNotFound());
  }

  @Test
  void shouldReturn400WhenCancellingInvalidStatus() throws Exception {
    UUID orderId = UUID.randomUUID();

    doThrow(
            new InvalidOrderStatusTransitionException(
                "Only PENDING_PAYMENT or PAID orders can be cancelled, current status: SHIPPED"))
        .when(cancelOrderUseCase)
        .execute(any(OrderId.class));

    mockMvc
        .perform(patch("/api/v1/orders/{id}/cancel", orderId))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldConfirmDelivery() throws Exception {
    UUID orderId = UUID.randomUUID();

    mockMvc
        .perform(patch("/api/v1/orders/{id}/confirm-delivery", orderId))
        .andExpect(status().isNoContent());

    verify(confirmDeliveryUseCase).execute(any(OrderId.class));
  }

  @Test
  void shouldReturn404WhenConfirmingDeliveryForNonExistentOrder() throws Exception {
    UUID orderId = UUID.randomUUID();

    doThrow(new OrderNotFoundException("The order not exist in our platform"))
        .when(confirmDeliveryUseCase)
        .execute(any(OrderId.class));

    mockMvc
        .perform(patch("/api/v1/orders/{id}/confirm-delivery", orderId))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldReturn400WhenConfirmingDeliveryForInvalidStatus() throws Exception {
    UUID orderId = UUID.randomUUID();

    doThrow(
            new InvalidOrderStatusTransitionException(
                "Only OUT_FOR_DELIVERY orders can be confirmed as delivered, current status: SHIPPED"))
        .when(confirmDeliveryUseCase)
        .execute(any(OrderId.class));

    mockMvc
        .perform(patch("/api/v1/orders/{id}/confirm-delivery", orderId))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldPrepareOrder() throws Exception {
    UUID orderId = UUID.randomUUID();

    mockMvc
        .perform(patch("/api/v1/orders/{id}/prepare", orderId))
        .andExpect(status().isNoContent());

    verify(prepareOrderUseCase).execute(any(OrderId.class));
  }

  @Test
  void shouldReturn404WhenPreparingNonExistentOrder() throws Exception {
    UUID orderId = UUID.randomUUID();

    doThrow(new OrderNotFoundException("The order not exist in our platform"))
        .when(prepareOrderUseCase)
        .execute(any(OrderId.class));

    mockMvc.perform(patch("/api/v1/orders/{id}/prepare", orderId)).andExpect(status().isNotFound());
  }

  @Test
  void shouldReturn400WhenPreparingInvalidStatus() throws Exception {
    UUID orderId = UUID.randomUUID();

    doThrow(
            new InvalidOrderStatusTransitionException(
                "Only PAID orders can be prepared, current status: PENDING_PAYMENT"))
        .when(prepareOrderUseCase)
        .execute(any(OrderId.class));

    mockMvc
        .perform(patch("/api/v1/orders/{id}/prepare", orderId))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldShipOrder() throws Exception {
    UUID orderId = UUID.randomUUID();

    mockMvc.perform(patch("/api/v1/orders/{id}/ship", orderId)).andExpect(status().isNoContent());

    verify(shipOrderUseCase).execute(any(OrderId.class));
  }

  @Test
  void shouldReturn404WhenShippingNonExistentOrder() throws Exception {
    UUID orderId = UUID.randomUUID();

    doThrow(new OrderNotFoundException("The order not exist in our platform"))
        .when(shipOrderUseCase)
        .execute(any(OrderId.class));

    mockMvc.perform(patch("/api/v1/orders/{id}/ship", orderId)).andExpect(status().isNotFound());
  }

  @Test
  void shouldReturn400WhenShippingInvalidStatus() throws Exception {
    UUID orderId = UUID.randomUUID();

    doThrow(
            new InvalidOrderStatusTransitionException(
                "Only IN_PREPARATION orders can be shipped, current status: PAID"))
        .when(shipOrderUseCase)
        .execute(any(OrderId.class));

    mockMvc.perform(patch("/api/v1/orders/{id}/ship", orderId)).andExpect(status().isBadRequest());
  }

  @Test
  void shouldMarkOrderOutForDelivery() throws Exception {
    UUID orderId = UUID.randomUUID();

    mockMvc
        .perform(patch("/api/v1/orders/{id}/out-for-delivery", orderId))
        .andExpect(status().isNoContent());

    verify(outForDeliveryUseCase).execute(any(OrderId.class));
  }

  @Test
  void shouldReturn404WhenMarkingOutForDeliveryNonExistentOrder() throws Exception {
    UUID orderId = UUID.randomUUID();

    doThrow(new OrderNotFoundException("The order not exist in our platform"))
        .when(outForDeliveryUseCase)
        .execute(any(OrderId.class));

    mockMvc
        .perform(patch("/api/v1/orders/{id}/out-for-delivery", orderId))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldReturn400WhenMarkingOutForDeliveryInvalidStatus() throws Exception {
    UUID orderId = UUID.randomUUID();

    doThrow(
            new InvalidOrderStatusTransitionException(
                "Only SHIPPED orders can be out for delivery, current status: IN_PREPARATION"))
        .when(outForDeliveryUseCase)
        .execute(any(OrderId.class));

    mockMvc
        .perform(patch("/api/v1/orders/{id}/out-for-delivery", orderId))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldUpdateTracking() throws Exception {
    UUID orderId = UUID.randomUUID();

    TrackingInfoRequest request = new TrackingInfoRequest();
    request.setTrackingNumber("TRK-99999");
    request.setShippingCarrier("Envia");
    request.setShippingLabelUrl("https://label.co/999");

    mockMvc
        .perform(
            patch("/api/v1/orders/{id}/tracking", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent());

    verify(updateTrackingUseCase)
        .execute(any(OrderId.class), eq("TRK-99999"), eq("Envia"), eq("https://label.co/999"));
  }

  @Test
  void shouldReturn404WhenUpdatingTrackingNonExistentOrder() throws Exception {
    UUID orderId = UUID.randomUUID();

    TrackingInfoRequest request = new TrackingInfoRequest();
    request.setTrackingNumber("TRK-111");
    request.setShippingCarrier("TCC");

    doThrow(new OrderNotFoundException("The order not exist in our platform"))
        .when(updateTrackingUseCase)
        .execute(any(OrderId.class), any(), any(), any());

    mockMvc
        .perform(
            patch("/api/v1/orders/{id}/tracking", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldRejectTrackingRequestWithoutTrackingNumber() throws Exception {
    UUID orderId = UUID.randomUUID();

    TrackingInfoRequest request = new TrackingInfoRequest();
    request.setShippingCarrier("TCC");

    mockMvc
        .perform(
            patch("/api/v1/orders/{id}/tracking", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldRejectTrackingRequestWithoutCarrier() throws Exception {
    UUID orderId = UUID.randomUUID();

    TrackingInfoRequest request = new TrackingInfoRequest();
    request.setTrackingNumber("TRK-111");

    mockMvc
        .perform(
            patch("/api/v1/orders/{id}/tracking", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldFindOrdersBySeller() throws Exception {
    UUID sellerId = UUID.randomUUID();
    UUID orderId1 = UUID.randomUUID();
    UUID orderId2 = UUID.randomUUID();

    List<Order> orders =
        List.of(
            buildOrder(orderId1, UUID.randomUUID(), OrderStatus.PAID, new BigDecimal("150000")),
            buildOrder(orderId2, UUID.randomUUID(), OrderStatus.SHIPPED, new BigDecimal("250000")));

    when(findBySellerUseCase.execute(sellerId)).thenReturn(orders);

    mockMvc
        .perform(get("/api/v1/orders/seller/{sellerId}", sellerId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].status").value("PAID"))
        .andExpect(jsonPath("$[1].status").value("SHIPPED"));
  }

  @Test
  void shouldReturnEmptyListWhenNoOrdersForSeller() throws Exception {
    UUID sellerId = UUID.randomUUID();

    when(findBySellerUseCase.execute(sellerId)).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/api/v1/orders/seller/{sellerId}", sellerId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  void shouldCompleteOrder() throws Exception {
    UUID orderId = UUID.randomUUID();

    mockMvc
        .perform(patch("/api/v1/orders/{id}/complete", orderId))
        .andExpect(status().isNoContent());

    verify(completeOrderUseCase).execute(any(OrderId.class));
  }

  @Test
  void shouldReturn404WhenCompletingNonExistentOrder() throws Exception {
    UUID orderId = UUID.randomUUID();

    doThrow(new OrderNotFoundException("The order not exist in our platform"))
        .when(completeOrderUseCase)
        .execute(any(OrderId.class));

    mockMvc
        .perform(patch("/api/v1/orders/{id}/complete", orderId))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldReturn400WhenCompletingInvalidStatus() throws Exception {
    UUID orderId = UUID.randomUUID();

    doThrow(
            new InvalidOrderStatusTransitionException(
                "Only DELIVERED orders can be completed, current status: SHIPPED"))
        .when(completeOrderUseCase)
        .execute(any(OrderId.class));

    mockMvc
        .perform(patch("/api/v1/orders/{id}/complete", orderId))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldDisputeOrder() throws Exception {
    UUID orderId = UUID.randomUUID();

    mockMvc
        .perform(patch("/api/v1/orders/{id}/dispute", orderId))
        .andExpect(status().isNoContent());

    verify(disputeOrderUseCase).execute(any(OrderId.class));
  }

  @Test
  void shouldReturn404WhenDisputingNonExistentOrder() throws Exception {
    UUID orderId = UUID.randomUUID();

    doThrow(new OrderNotFoundException("The order not exist in our platform"))
        .when(disputeOrderUseCase)
        .execute(any(OrderId.class));

    mockMvc.perform(patch("/api/v1/orders/{id}/dispute", orderId)).andExpect(status().isNotFound());
  }

  @Test
  void shouldReturn400WhenDisputingInvalidStatus() throws Exception {
    UUID orderId = UUID.randomUUID();

    doThrow(
            new InvalidOrderStatusTransitionException(
                "Cannot open dispute for order with status: PENDING_PAYMENT"))
        .when(disputeOrderUseCase)
        .execute(any(OrderId.class));

    mockMvc
        .perform(patch("/api/v1/orders/{id}/dispute", orderId))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldRefundOrder() throws Exception {
    UUID orderId = UUID.randomUUID();

    mockMvc.perform(patch("/api/v1/orders/{id}/refund", orderId)).andExpect(status().isNoContent());

    verify(refundOrderUseCase).execute(any(OrderId.class));
  }

  @Test
  void shouldReturn404WhenRefundingNonExistentOrder() throws Exception {
    UUID orderId = UUID.randomUUID();

    doThrow(new OrderNotFoundException("The order not exist in our platform"))
        .when(refundOrderUseCase)
        .execute(any(OrderId.class));

    mockMvc.perform(patch("/api/v1/orders/{id}/refund", orderId)).andExpect(status().isNotFound());
  }

  @Test
  void shouldReturn400WhenRefundingInvalidStatus() throws Exception {
    UUID orderId = UUID.randomUUID();

    doThrow(
            new InvalidOrderStatusTransitionException(
                "Only DISPUTE orders can be refunded, current status: SHIPPED"))
        .when(refundOrderUseCase)
        .execute(any(OrderId.class));

    mockMvc
        .perform(patch("/api/v1/orders/{id}/refund", orderId))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldGetSellerSummary() throws Exception {
    UUID sellerId = UUID.randomUUID();

    OrderSummaryResponse summary =
        new OrderSummaryResponse(
            sellerId,
            5,
            new BigDecimal("1500000"),
            Map.of("PAID", 2, "SHIPPED", 1, "DELIVERED", 2),
            new BigDecimal("300000.00"));

    when(orderSummaryUseCase.execute(sellerId)).thenReturn(summary);

    mockMvc
        .perform(get("/api/v1/orders/seller/{sellerId}/summary", sellerId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.sellerId").value(sellerId.toString()))
        .andExpect(jsonPath("$.totalOrders").value(5))
        .andExpect(jsonPath("$.totalRevenue").value(1500000))
        .andExpect(jsonPath("$.averageOrderValue").value(300000))
        .andExpect(jsonPath("$.ordersByStatus.PAID").value(2))
        .andExpect(jsonPath("$.ordersByStatus.SHIPPED").value(1))
        .andExpect(jsonPath("$.ordersByStatus.DELIVERED").value(2));
  }

  @Test
  void shouldGetTrackingEvents() throws Exception {
    UUID orderId = UUID.randomUUID();

    when(getTrackingEventsUseCase.execute(any(OrderId.class))).thenReturn(List.of());

    mockMvc
        .perform(get("/api/v1/orders/{id}/tracking", orderId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  void shouldAddTrackingEvent() throws Exception {
    UUID orderId = UUID.randomUUID();

    TrackingEventRequest request = new TrackingEventRequest();
    request.setStatus("IN_TRANSIT");
    request.setLocation("Bogota");
    request.setDescription("Package in transit");
    request.setEventTimestamp(Timestamp.from(Instant.now()));

    TrackingEvent savedEvent =
        TrackingEvent.create(
            orderId, "IN_TRANSIT", "Bogota", "Package in transit", request.getEventTimestamp());

    when(addTrackingEventUseCase.execute(any(OrderId.class), any(), any(), any(), any()))
        .thenReturn(savedEvent);

    mockMvc
        .perform(
            post("/api/v1/orders/{id}/tracking", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("IN_TRANSIT"))
        .andExpect(jsonPath("$.location").value("Bogota"));
  }

  @Test
  void shouldReturn404WhenAddingTrackingEventToNonExistentOrder() throws Exception {
    UUID orderId = UUID.randomUUID();

    TrackingEventRequest request = new TrackingEventRequest();
    request.setStatus("IN_TRANSIT");
    request.setLocation("Bogota");
    request.setEventTimestamp(Timestamp.from(Instant.now()));

    when(addTrackingEventUseCase.execute(any(OrderId.class), any(), any(), any(), any()))
        .thenThrow(new OrderNotFoundException("The order not exist in our platform"));

    mockMvc
        .perform(
            post("/api/v1/orders/{id}/tracking", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldRejectTrackingEventRequestWithoutStatus() throws Exception {
    UUID orderId = UUID.randomUUID();

    TrackingEventRequest request = new TrackingEventRequest();
    request.setLocation("Bogota");
    request.setEventTimestamp(Timestamp.from(Instant.now()));

    mockMvc
        .perform(
            post("/api/v1/orders/{id}/tracking", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldGetStatusCounts() throws Exception {
    UUID sellerId = UUID.randomUUID();

    OrderStatusCountResponse counts =
        new OrderStatusCountResponse(sellerId, 10, Map.of("PAID", 5, "SHIPPED", 3, "DELIVERED", 2));

    when(orderStatusCountsUseCase.execute(sellerId)).thenReturn(counts);

    mockMvc
        .perform(get("/api/v1/orders/seller/{sellerId}/status-counts", sellerId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.sellerId").value(sellerId.toString()))
        .andExpect(jsonPath("$.totalOrders").value(10))
        .andExpect(jsonPath("$.counts.PAID").value(5))
        .andExpect(jsonPath("$.counts.SHIPPED").value(3))
        .andExpect(jsonPath("$.counts.DELIVERED").value(2));
  }

  @Test
  void shouldSearchOrders() throws Exception {
    UUID sellerId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    Order order = buildOrder(orderId, customerId, OrderStatus.PAID, new BigDecimal("150000"));

    when(searchOrdersUseCase.execute(sellerId, OrderStatus.PAID, 0, 10))
        .thenReturn(new SearchOrdersUseCase.SearchResult(List.of(order), 1, 0, 10, 1));

    mockMvc
        .perform(
            get("/api/v1/orders/seller/{sellerId}/search", sellerId)
                .param("status", "PAID")
                .param("page", "0")
                .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.totalPages").value(1))
        .andExpect(jsonPath("$.orders[0].status").value("PAID"));
  }

  @Test
  void shouldSearchOrdersWithoutStatusFilter() throws Exception {
    UUID sellerId = UUID.randomUUID();

    when(searchOrdersUseCase.execute(sellerId, null, 0, 10))
        .thenReturn(new SearchOrdersUseCase.SearchResult(List.of(), 0, 0, 10, 0));

    mockMvc
        .perform(
            get("/api/v1/orders/seller/{sellerId}/search", sellerId)
                .param("page", "0")
                .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(0));
  }

  private Order buildOrder(
      UUID orderId, UUID customerId, OrderStatus status, BigDecimal totalAmount) {
    return new Order(
        new OrderId(orderId),
        new OrderCustomerId(customerId),
        status,
        new OrderTotalAmount(totalAmount),
        new OrderShippingAddress("Calle 100 #15-20"),
        new OrderShippingDepartment("Bogota"),
        new OrderShippingCity("Bogota D.C."),
        new OrderCreatedAt(Timestamp.from(Instant.now())),
        null,
        null,
        null,
        null);
  }
}
