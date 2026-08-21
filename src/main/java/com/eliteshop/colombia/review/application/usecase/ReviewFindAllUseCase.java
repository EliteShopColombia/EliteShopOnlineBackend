package com.eliteshop.colombia.review.application.usecase;

import com.eliteshop.colombia.review.domain.model.Review;
import com.eliteshop.colombia.review.domain.repository.ReviewRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Slf4j
@RequiredArgsConstructor
public class ReviewFindAllUseCase {

  private final ReviewRepository repository;

  public List<Review> execute() {
    log.info("Buscando todas las reseñas");
    List<Review> reviews = repository.findAll();
    log.info("Se encontraron {} reseñas", reviews.size());
    return reviews;
  }

  public Page<Review> execute(Pageable pageable) {
    log.info("Buscando reseñas con paginación: página {}", pageable.getPageNumber());
    Page<Review> reviews = repository.findAll(pageable);
    log.info(
        "Se encontraron {} reseñas en la página {}",
        reviews.getNumberOfElements(),
        pageable.getPageNumber());
    return reviews;
  }
}
