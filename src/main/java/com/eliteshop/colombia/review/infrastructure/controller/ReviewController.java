package com.eliteshop.colombia.review.infrastructure.controller;

import com.eliteshop.colombia.review.application.usecase.*;
import com.eliteshop.colombia.review.domain.model.Review;
import com.eliteshop.colombia.review.domain.model.ReviewId;
import com.eliteshop.colombia.review.domain.model.ReviewImage;
import com.eliteshop.colombia.review.domain.model.ReviewImageId;
import com.eliteshop.colombia.review.domain.model.ReviewImageOrder;
import com.eliteshop.colombia.review.domain.model.ReviewImageUrl;
import com.eliteshop.colombia.review.domain.model.ReviewProductId;
import com.eliteshop.colombia.review.domain.repository.ReviewImageRepository;
import com.eliteshop.colombia.review.infrastructure.adapter.ReviewMinIOAdapter;
import com.eliteshop.colombia.review.infrastructure.controller.dto.ReviewRequest;
import com.eliteshop.colombia.review.infrastructure.controller.dto.ReviewResponse;
import com.eliteshop.colombia.review.infrastructure.mapper.ReviewMapper;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
  private final ReviewMinIOAdapter minIOAdapter;
  private final ReviewImageRepository reviewImageRepository;
  private final com.eliteshop.colombia.shared.security.AuthorizationService authorizationService;

  @PostMapping("/reviews")
  public ResponseEntity<ReviewResponse> save(
      @Valid @RequestPart("review") ReviewRequest request,
      @RequestPart(value = "images", required = false) List<MultipartFile> images,
      Authentication authentication) {
    Review review =
        mapper.toDomainFromRequest(
            request, authorizationService.authenticatedUserId(authentication));
    saveUseCase.execute(review);

    if (images != null && !images.isEmpty()) {
      List<ReviewImage> reviewImages = uploadReviewImages(review.getId(), images);
      for (ReviewImage image : reviewImages) {
        reviewImageRepository.save(image);
      }
      review =
          new Review(
              review.getId(),
              review.getProductId(),
              review.getCustomerId(),
              review.getQualify(),
              review.getContent(),
              reviewImages);
    }

    return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(review));
  }

  @DeleteMapping("/reviews/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication authentication) {
    Review review = findByIdUseCase.execute(new ReviewId(id)).orElseThrow();
    authorizationService.requireCustomer(authentication, review.getCustomerId().getValue());
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

  private List<ReviewImage> uploadReviewImages(ReviewId reviewId, List<MultipartFile> images) {
    List<ReviewImage> reviewImages = new ArrayList<>();
    int order = 1;

    for (MultipartFile file : images) {
      if (order > 3) {
        break;
      }

      try {
        String imageUrl =
            minIOAdapter.uploadImage(
                reviewId.getValue().toString(), file.getOriginalFilename(), file.getInputStream());
        ReviewImage image =
            new ReviewImage(
                ReviewImageId.generate(),
                reviewId,
                new ReviewImageUrl(imageUrl),
                new ReviewImageOrder(order));
        reviewImages.add(image);
        order++;
      } catch (Exception e) {
        throw new com.eliteshop.colombia.shared.exception.StorageException(
            "Error subiendo imagen de review", e);
      }
    }

    return reviewImages;
  }
}
