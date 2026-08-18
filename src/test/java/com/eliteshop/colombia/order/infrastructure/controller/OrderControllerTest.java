package com.eliteshop.colombia.order.infrastructure.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.eliteshop.colombia.order.application.*;
import com.eliteshop.colombia.order.domain.exception.OrderAlreadyExistsException;
import com.eliteshop.colombia.order.domain.exception.OrderNotFoundException;
import com.eliteshop.colombia.order.domain.model.*;
import com.eliteshop.colombia.order.infrastructure.controller.dto.OrderRequest;
import com.eliteshop.colombia.order.infrastructure.mapper.OrderMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
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
  private OrderMapper mapper;

  @BeforeEach
  void setUp() {
    saveUseCase = mock(OrderSaveUseCase.class);
    updateUseCase = mock(OrderUpdateUseCase.class);
    deleteUseCase = mock(OrderDeleteUseCase.class);
    findAllUseCase = mock(OrderFindAllUseCase.class);
    findByIdUseCase = mock(OrderFindByIdUseCase.class);
    mapper = new OrderMapper();

    OrderController controller =
        new OrderController(
            saveUseCase, updateUseCase, deleteUseCase, findAllUseCase, findByIdUseCase, mapper);
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
        .andExpect(jsonPath("$.status").value("PENDING"))
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
            buildOrder(orderId1, customerId, OrderStatus.PENDING, new BigDecimal("100000")),
            buildOrder(orderId2, customerId, OrderStatus.CONFIRMED, new BigDecimal("200000")));

    when(findAllUseCase.execute()).thenReturn(orders);

    mockMvc
        .perform(get("/api/v1/orders"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].customerId").value(customerId.toString()))
        .andExpect(jsonPath("$[0].status").value("PENDING"))
        .andExpect(jsonPath("$[1].status").value("CONFIRMED"));
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

    Order order = buildOrder(orderId, customerId, OrderStatus.PENDING, new BigDecimal("300000"));

    when(findByIdUseCase.execute(any(OrderId.class))).thenReturn(Optional.of(order));

    mockMvc
        .perform(get("/api/v1/orders/{id}", orderId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(orderId.toString()))
        .andExpect(jsonPath("$.customerId").value(customerId.toString()))
        .andExpect(jsonPath("$.status").value("PENDING"))
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
    request.setStatus("CONFIRMED");

    Order updatedOrder =
        buildOrder(orderId, customerId, OrderStatus.CONFIRMED, new BigDecimal("400000"));

    when(findByIdUseCase.execute(any(OrderId.class))).thenReturn(Optional.of(updatedOrder));

    mockMvc
        .perform(
            put("/api/v1/orders/{id}", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(orderId.toString()))
        .andExpect(jsonPath("$.status").value("CONFIRMED"))
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
        null);
  }
}
