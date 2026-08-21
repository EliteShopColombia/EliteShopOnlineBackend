package com.eliteshop.colombia.order.infrastructure.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.eliteshop.colombia.order.domain.model.*;
import com.eliteshop.colombia.order.domain.repository.OrderItemRepository;
import com.eliteshop.colombia.order.infrastructure.controller.dto.OrderRequest;
import com.eliteshop.colombia.order.infrastructure.controller.dto.OrderResponse;
import com.eliteshop.colombia.order.infrastructure.persistence.OrderEntity;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderMapperTest {

  private OrderMapper mapper;

  @BeforeEach
  void setUp() {
    OrderItemRepository orderItemRepository = mock(OrderItemRepository.class);
    mapper = new OrderMapper(orderItemRepository);
  }

  @Test
  void shouldMapEntityToDomain() {
    OrderEntity entity = new OrderEntity();
    entity.setId(UUID.randomUUID());
    entity.setCustomerId(UUID.randomUUID());
    entity.setStatus("PENDING_PAYMENT");
    entity.setTotalAmount(new BigDecimal("200000"));
    entity.setShippingAddress("Calle 100 #15-20");
    entity.setShippingDepartment("Bogota");
    entity.setShippingCity("Bogota D.C.");
    entity.setCreatedAt(Timestamp.from(Instant.now()));
    entity.setUpdatedAt(null);

    Order domain = mapper.toDomain(entity);

    assertThat(domain).isNotNull();
    assertThat(domain.getId().getValue()).isEqualTo(entity.getId());
    assertThat(domain.getCustomerId().getValue()).isEqualTo(entity.getCustomerId());
    assertThat(domain.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
    assertThat(domain.getTotalAmount().getValue()).isEqualByComparingTo(entity.getTotalAmount());
    assertThat(domain.getShippingAddress().getValue()).isEqualTo("Calle 100 #15-20");
    assertThat(domain.getShippingDepartment().getValue()).isEqualTo("Bogota");
    assertThat(domain.getShippingCity().getValue()).isEqualTo("Bogota D.C.");
    assertThat(domain.getCreatedAt().getValue()).isEqualTo(entity.getCreatedAt());
    assertThat(domain.getUpdatedAt()).isNull();
  }

  @Test
  void shouldMapEntityToDomainWithUpdatedAt() {
    OrderEntity entity = new OrderEntity();
    entity.setId(UUID.randomUUID());
    entity.setCustomerId(UUID.randomUUID());
    entity.setStatus("PAID");
    entity.setTotalAmount(new BigDecimal("150000"));
    entity.setShippingAddress("Carrera 7 #32-16");
    entity.setShippingDepartment("Antioquia");
    entity.setShippingCity("Medellin");
    entity.setCreatedAt(Timestamp.from(Instant.now().minusSeconds(3600)));
    entity.setUpdatedAt(Timestamp.from(Instant.now()));

    Order domain = mapper.toDomain(entity);

    assertThat(domain.getUpdatedAt()).isNotNull();
    assertThat(domain.getUpdatedAt().getValue()).isEqualTo(entity.getUpdatedAt());
  }

  @Test
  void shouldReturnNullWhenEntityIsNull() {
    assertThat(mapper.toDomain(null)).isNull();
  }

  @Test
  void shouldMapDomainToEntity() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();
    Timestamp now = Timestamp.from(Instant.now());

    Order domain =
        new Order(
            new OrderId(orderId),
            new OrderCustomerId(customerId),
            OrderStatus.PAID,
            new OrderTotalAmount(new BigDecimal("350000")),
            new OrderShippingAddress("Avenida Caracas #30-15"),
            new OrderShippingDepartment("Cundinamarca"),
            new OrderShippingCity("Bogota D.C."),
            new OrderCreatedAt(now),
            null,
            null,
            null,
            null);

    OrderEntity entity = mapper.toEntity(domain);

    assertThat(entity).isNotNull();
    assertThat(entity.getId()).isEqualTo(orderId);
    assertThat(entity.getCustomerId()).isEqualTo(customerId);
    assertThat(entity.getStatus()).isEqualTo("PAID");
    assertThat(entity.getTotalAmount()).isEqualByComparingTo(new BigDecimal("350000"));
    assertThat(entity.getShippingAddress()).isEqualTo("Avenida Caracas #30-15");
    assertThat(entity.getShippingDepartment()).isEqualTo("Cundinamarca");
    assertThat(entity.getShippingCity()).isEqualTo("Bogota D.C.");
    assertThat(entity.getCreatedAt()).isEqualTo(now);
    assertThat(entity.getUpdatedAt()).isNull();
  }

  @Test
  void shouldMapDomainToEntityWithUpdatedAt() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();
    Timestamp now = Timestamp.from(Instant.now());
    Timestamp updatedAt = Timestamp.from(Instant.now());

    Order domain =
        new Order(
            new OrderId(orderId),
            new OrderCustomerId(customerId),
            OrderStatus.SHIPPED,
            new OrderTotalAmount(new BigDecimal("500000")),
            new OrderShippingAddress("Calle 85 #11-50"),
            new OrderShippingDepartment("Bogota"),
            new OrderShippingCity("Bogota D.C."),
            new OrderCreatedAt(now),
            new OrderUpdatedAt(updatedAt),
            null,
            null,
            null);

    OrderEntity entity = mapper.toEntity(domain);

    assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
  }

  @Test
  void shouldReturnNullWhenDomainIsNull() {
    assertThat(mapper.toEntity(null)).isNull();
  }

  @Test
  void shouldMapRequestToDomain() {
    OrderRequest request = new OrderRequest();
    request.setCustomerId(UUID.randomUUID());
    request.setTotalAmount(new BigDecimal("180000"));
    request.setShippingAddress("Calle 50 #10-25");
    request.setShippingDepartment("Valle del Cauca");
    request.setShippingCity("Cali");

    Order domain = mapper.toDomainFromRequest(request);

    assertThat(domain).isNotNull();
    assertThat(domain.getId()).isNotNull();
    assertThat(domain.getId().getValue()).isNotNull();
    assertThat(domain.getCustomerId().getValue()).isEqualTo(request.getCustomerId());
    assertThat(domain.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
    assertThat(domain.getTotalAmount().getValue()).isEqualByComparingTo(new BigDecimal("180000"));
    assertThat(domain.getShippingAddress().getValue()).isEqualTo("Calle 50 #10-25");
    assertThat(domain.getShippingDepartment().getValue()).isEqualTo("Valle del Cauca");
    assertThat(domain.getShippingCity().getValue()).isEqualTo("Cali");
    assertThat(domain.getCreatedAt()).isNotNull();
    assertThat(domain.getUpdatedAt()).isNull();
  }

  @Test
  void shouldReturnNullWhenRequestIsNull() {
    assertThat(mapper.toDomainFromRequest(null)).isNull();
  }

  @Test
  void shouldMapDomainToResponse() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();
    Timestamp now = Timestamp.from(Instant.now());

    Order domain =
        new Order(
            new OrderId(orderId),
            new OrderCustomerId(customerId),
            OrderStatus.DELIVERED,
            new OrderTotalAmount(new BigDecimal("420000")),
            new OrderShippingAddress("Carrera 15 #80-50"),
            new OrderShippingDepartment("Bogota"),
            new OrderShippingCity("Usaquen"),
            new OrderCreatedAt(now),
            null,
            null,
            null,
            null);

    OrderResponse response = mapper.toResponse(domain);

    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(orderId);
    assertThat(response.getCustomerId()).isEqualTo(customerId);
    assertThat(response.getStatus()).isEqualTo("DELIVERED");
    assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("420000"));
    assertThat(response.getShippingAddress()).isEqualTo("Carrera 15 #80-50");
    assertThat(response.getShippingDepartment()).isEqualTo("Bogota");
    assertThat(response.getShippingCity()).isEqualTo("Usaquen");
    assertThat(response.getCreatedAt()).isEqualTo(now);
    assertThat(response.getUpdatedAt()).isNull();
  }

  @Test
  void shouldMapDomainToResponseWithUpdatedAt() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();
    Timestamp now = Timestamp.from(Instant.now());
    Timestamp updatedAt = Timestamp.from(Instant.now());

    Order domain =
        new Order(
            new OrderId(orderId),
            new OrderCustomerId(customerId),
            OrderStatus.CANCELLED,
            new OrderTotalAmount(new BigDecimal("75000")),
            new OrderShippingAddress("Calle 72 #10-40"),
            new OrderShippingDepartment("Bogota"),
            new OrderShippingCity("Chapinero"),
            new OrderCreatedAt(now),
            new OrderUpdatedAt(updatedAt),
            null,
            null,
            null);

    OrderResponse response = mapper.toResponse(domain);

    assertThat(response.getUpdatedAt()).isEqualTo(updatedAt);
  }

  @Test
  void shouldReturnNullWhenResponseDomainIsNull() {
    assertThat(mapper.toResponse(null)).isNull();
  }

  @Test
  void shouldPreserveAllStatusValues() {
    for (OrderStatus status : OrderStatus.values()) {
      UUID orderId = UUID.randomUUID();
      UUID customerId = UUID.randomUUID();

      Order domain =
          new Order(
              new OrderId(orderId),
              new OrderCustomerId(customerId),
              status,
              new OrderTotalAmount(new BigDecimal("100000")),
              new OrderShippingAddress("Calle 100"),
              new OrderShippingDepartment("Bogota"),
              new OrderShippingCity("Bogota D.C."),
              new OrderCreatedAt(Timestamp.from(Instant.now())),
              null,
              null,
              null,
              null);

      OrderEntity entity = mapper.toEntity(domain);
      Order mappedBack = mapper.toDomain(entity);

      assertThat(mappedBack.getStatus()).isEqualTo(status);
    }
  }
}
