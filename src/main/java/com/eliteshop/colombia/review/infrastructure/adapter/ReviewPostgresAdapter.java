package com.eliteshop.colombia.review.infrastructure.adapter;

import com.eliteshop.colombia.review.domain.model.Review;
import com.eliteshop.colombia.review.domain.model.ReviewId;
import com.eliteshop.colombia.review.domain.model.ReviewProductId;
import com.eliteshop.colombia.review.domain.repository.ReviewRepository;
import com.eliteshop.colombia.review.infrastructure.mapper.ReviewMapper;
import com.eliteshop.colombia.review.infrastructure.persistence.ReviewEntity;
import com.eliteshop.colombia.review.infrastructure.persistence.ReviewJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewPostgresAdapter implements ReviewRepository {

  private final ReviewJpaRepository jpaRepository;
  private final ReviewMapper mapper;

  @Override
  public Review save(Review review) {
    ReviewEntity entity = mapper.toEntity(review);
    ReviewEntity savedEntity = jpaRepository.save(entity);
    return mapper.toDomain(savedEntity);
  }

  @Override
  public void delete(ReviewId id) {
    jpaRepository.deleteById(id.getValue());
  }

  @Override
  public List<Review> findAll() {
    return jpaRepository.findAll().stream().map(mapper::toDomain).collect(Collectors.toList());
  }

  @Override
  public Page<Review> findAll(Pageable pageable) {
    return jpaRepository.findAll(pageable).map(mapper::toDomain);
  }

  @Override
  public Optional<Review> findById(ReviewId id) {
    return jpaRepository.findById(id.getValue()).map(mapper::toDomain);
  }

  @Override
  public List<Review> findByProductId(ReviewProductId productId) {
    return jpaRepository.findByProductId(productId.getValue()).stream()
        .map(mapper::toDomain)
        .collect(Collectors.toList());
  }
}
