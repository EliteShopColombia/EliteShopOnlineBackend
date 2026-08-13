package com.eliteshop.colombia.product.application.usecase;

import com.eliteshop.colombia.product.domain.model.Product;
import com.eliteshop.colombia.product.domain.repository.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class ProductFindAllUseCase {

  private final ProductRepository repository;

  public List<Product> execute() {
    return repository.findAll();
  }

  public Page<Product> execute(Pageable pageable) {
    return repository.findAll(pageable);
  }
}
