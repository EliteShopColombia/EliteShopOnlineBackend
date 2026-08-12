package com.eliteshop.colombia.review.infrastructure.controller;

import com.eliteshop.colombia.review.application.usecase.*;
import com.eliteshop.colombia.review.domain.model.Review;
import com.eliteshop.colombia.review.domain.model.ReviewId;
import com.eliteshop.colombia.review.domain.model.ReviewProductId;
import com.eliteshop.colombia.review.infrastructure.controller.dto.ReviewRequest;
import com.eliteshop.colombia.review.infrastructure.controller.dto.ReviewResponse;
import com.eliteshop.colombia.review.infrastructure.mapper.ReviewMapper;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReviewController {

  private final ReviewSaveUseCase saveUseCase;
  private final ReviewDeleteUseCase deleteUseCase;
  private final ReviewFindAllUseCase findAllUseCase;
  private final ReviewFindByIdUseCase findByIdUseCase;
  private final ReviewFindByProductIdUseCase findByProductIdUseCase;
  private final ReviewMapper mapper;

  @PostMapping("/reviews")
  public ResponseEntity<ReviewResponse> save(@Valid @RequestBody ReviewRequest request) {
    Review review = mapper.toDomainFromRequest(request);
    saveUseCase.execute(review);
    return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(review));
  }

  @DeleteMapping("/reviews/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    deleteUseCase.execute(new ReviewId(id));
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/reviews")
  public ResponseEntity<Page<ReviewResponse>> findAll(
      @PageableDefault(size = 25) Pageable pageable) {
    Page<Review> reviews = findAllUseCase.execute(pageable);
    Page<ReviewResponse> responses = reviews.map(mapper::toResponse);
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/reviews/{id}")
  public ResponseEntity<ReviewResponse> findById(@PathVariable UUID id) {
    return findByIdUseCase
        .execute(new ReviewId(id))
        .map(review -> ResponseEntity.ok(mapper.toResponse(review)))
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/reviews/product/{productId}")
  public ResponseEntity<List<ReviewResponse>> findByProductId(@PathVariable UUID productId) {
    List<Review> reviews = findByProductIdUseCase.execute(new ReviewProductId(productId));
    List<ReviewResponse> responses =
        reviews.stream().map(mapper::toResponse).collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }
}
