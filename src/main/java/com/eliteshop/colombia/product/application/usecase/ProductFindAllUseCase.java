package com.eliteshop.colombia.product.application.usecase;

import com.eliteshop.colombia.product.domain.model.Product;
import com.eliteshop.colombia.product.domain.repository.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Slf4j
@RequiredArgsConstructor
public class ProductFindAllUseCase {

  private final ProductRepository repository;

  public List<Product> execute() {
    log.info("Listando todos los productos");
    List<Product> products = repository.findAll();
    log.info("Se encontraron {} productos", products.size());
    return products;
  }

  public Page<Product> execute(Pageable pageable) {
    log.info("Listando productos con paginación");
    Page<Product> page = repository.findAll(pageable);
    log.info("Se encontraron {} productos en la página", page.getNumberOfElements());
    return page;
  }
}
