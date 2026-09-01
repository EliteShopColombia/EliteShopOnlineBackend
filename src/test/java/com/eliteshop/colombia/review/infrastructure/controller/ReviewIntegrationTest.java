package com.eliteshop.colombia.review.infrastructure.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.eliteshop.colombia.review.domain.model.*;
import com.eliteshop.colombia.review.domain.repository.ReviewRepository;
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
class ReviewIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ReviewRepository reviewRepository;

  private UUID reviewId;
  private UUID productId;
  private UUID customerId;

  @BeforeEach
  void setUp() {
    reviewId = UUID.randomUUID();
    productId = UUID.randomUUID();
    customerId = UUID.randomUUID();
    reviewRepository.save(
        new Review(
            new ReviewId(reviewId),
            new ReviewProductId(productId),
            new ReviewCustomerId(customerId),
            new ReviewQualify(5),
            new ReviewContent("Excelente producto"),
            null));
  }

  @AfterEach
  void cleanUp() {
    reviewRepository.delete(new ReviewId(reviewId));
  }

  @Test
  void shouldListAllReviews() throws Exception {
    mockMvc.perform(get("/api/v1/reviews")).andExpect(status().isOk());
  }

  @Test
  void shouldGetReviewById() throws Exception {
    mockMvc
        .perform(get("/api/v1/reviews/{id}", reviewId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").value("Excelente producto"));
  }

  @Test
  void shouldReturn404ForNonExistentReview() throws Exception {
    UUID nonExistent = UUID.randomUUID();

    mockMvc.perform(get("/api/v1/reviews/{id}", nonExistent)).andExpect(status().isNotFound());
  }

  @Test
  void shouldFindReviewsByProductId() throws Exception {
    mockMvc
        .perform(get("/api/v1/reviews/product/{productId}", productId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }
}
