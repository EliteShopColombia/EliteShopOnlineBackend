package com.eliteshop.colombia.review.application.usecase;

import com.eliteshop.colombia.review.domain.model.Review;
import com.eliteshop.colombia.review.domain.repository.ReviewRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class ReviewFindAllUseCase {

  private final ReviewRepository repository;

  public List<Review> execute() {
    return repository.findAll();
  }

  public Page<Review> execute(Pageable pageable) {
    return repository.findAll(pageable);
  }
}
