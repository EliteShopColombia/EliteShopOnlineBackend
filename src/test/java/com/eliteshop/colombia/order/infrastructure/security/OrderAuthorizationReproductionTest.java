package com.eliteshop.colombia.order.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eliteshop.colombia.customer.domain.model.Customer;
import com.eliteshop.colombia.customer.domain.model.CustomerCreatedAt;
import com.eliteshop.colombia.customer.domain.model.CustomerEmail;
import com.eliteshop.colombia.customer.domain.model.CustomerFirstName;
import com.eliteshop.colombia.customer.domain.model.CustomerId;
import com.eliteshop.colombia.customer.domain.model.CustomerLastName;
import com.eliteshop.colombia.customer.domain.model.CustomerPassword;
import com.eliteshop.colombia.customer.domain.model.CustomerPhoneNumber;
import com.eliteshop.colombia.customer.domain.repository.CustomerRepository;
import com.eliteshop.colombia.order.domain.model.Order;
import com.eliteshop.colombia.order.domain.model.OrderCreatedAt;
import com.eliteshop.colombia.order.domain.model.OrderCustomerId;
import com.eliteshop.colombia.order.domain.model.OrderId;
import com.eliteshop.colombia.order.domain.model.OrderShippingAddress;
import com.eliteshop.colombia.order.domain.model.OrderShippingCity;
import com.eliteshop.colombia.order.domain.model.OrderShippingDepartment;
import com.eliteshop.colombia.order.domain.model.OrderStatus;
import com.eliteshop.colombia.order.domain.model.OrderTotalAmount;
import com.eliteshop.colombia.order.domain.repository.OrderRepository;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class OrderAuthorizationReproductionTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private OrderRepository orderRepository;
  @Autowired private CustomerRepository customerRepository;

  private UUID createdOrderId;
  private UUID createdCustomerId;

  @AfterEach
  void cleanUp() {
    if (createdOrderId != null) {
      orderRepository.delete(new OrderId(createdOrderId));
    }
    if (createdCustomerId != null) {
      customerRepository.delete(
          new com.eliteshop.colombia.customer.domain.model.CustomerId(createdCustomerId));
    }
  }

  @Test
  @WithMockUser(username = "00000000-0000-0000-0000-000000000001", roles = "CUSTOMER")
  void customerCannotDisputeAnotherCustomersCompletedOrder() throws Exception {
    UUID victimId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    createdCustomerId = victimId;
    createdOrderId = orderId;
    saveCustomer(victimId);
    orderRepository.save(buildOrder(orderId, victimId, OrderStatus.COMPLETED));

    mockMvc
        .perform(patch("/api/v1/orders/{id}/dispute", orderId))
        .andExpect(status().isForbidden());

    assertThat(orderRepository.findById(new OrderId(orderId)).orElseThrow().getStatus())
        .isEqualTo(OrderStatus.COMPLETED);
  }

  @Test
  @WithMockUser(username = "00000000-0000-0000-0000-000000000001", roles = "CUSTOMER")
  void customerCannotRefundAnotherCustomersDisputedOrder() throws Exception {
    UUID victimId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    createdCustomerId = victimId;
    createdOrderId = orderId;
    saveCustomer(victimId);
    orderRepository.save(buildOrder(orderId, victimId, OrderStatus.DISPUTE));

    mockMvc.perform(patch("/api/v1/orders/{id}/refund", orderId)).andExpect(status().isForbidden());

    assertThat(orderRepository.findById(new OrderId(orderId)).orElseThrow().getStatus())
        .isEqualTo(OrderStatus.DISPUTE);
  }

  private Order buildOrder(UUID orderId, UUID customerId, OrderStatus status) {
    return new Order(
        new OrderId(orderId),
        new OrderCustomerId(customerId),
        status,
        new OrderTotalAmount(new BigDecimal("200000")),
        new OrderShippingAddress("Calle 100 #15-20"),
        new OrderShippingDepartment("Bogota"),
        new OrderShippingCity("Bogota D.C."),
        new OrderCreatedAt(Timestamp.from(Instant.now())),
        null,
        null,
        null,
        null);
  }

  private void saveCustomer(UUID customerId) {
    customerRepository.save(
        new Customer(
            new CustomerId(customerId),
            new CustomerFirstName("Victim"),
            new CustomerLastName("Customer"),
            new CustomerEmail(customerId + "@example.com"),
            new CustomerPhoneNumber("3001234567"),
            new CustomerPassword("password"),
            null,
            new CustomerCreatedAt(Timestamp.from(Instant.now())),
            null,
            null));
  }
}
