package com.eliteshop.colombia.product.infrastructure.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.eliteshop.colombia.product.domain.model.*;
import com.eliteshop.colombia.product.domain.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ProductRepository productRepository;

  private UUID productId;
  private UUID sellerId;

  @BeforeEach
  void setUp() {
    sellerId = UUID.randomUUID();
    productId = UUID.randomUUID();
    productRepository.save(
        new Product(
            new ProductId(productId),
            new ProductSellerId(sellerId),
            new ProductName("Camiseta Test"),
            new ProductPrice(new BigDecimal("50000")),
            new ProductStock(10)));
  }

  @AfterEach
  void cleanUp() {
    productRepository.delete(new ProductId(productId));
  }

  @Test
  void shouldListAllProducts() throws Exception {
    mockMvc
        .perform(get("/api/v1/products"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }

  @Test
  void shouldGetProductById() throws Exception {
    mockMvc
        .perform(get("/api/v1/products/{id}", productId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Camiseta Test"))
        .andExpect(jsonPath("$.price").value(50000));
  }

  @Test
  void shouldReturn404ForNonExistentProduct() throws Exception {
    UUID nonExistent = UUID.randomUUID();

    mockMvc.perform(get("/api/v1/products/{id}", nonExistent)).andExpect(status().isNotFound());
  }
}
