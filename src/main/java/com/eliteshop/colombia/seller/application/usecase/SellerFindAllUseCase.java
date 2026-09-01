package com.eliteshop.colombia.seller.application.usecase;

import com.eliteshop.colombia.seller.domain.model.Seller;
import com.eliteshop.colombia.seller.domain.repository.SellerRepository;
import com.eliteshop.colombia.shared.domain.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SellerFindAllUseCase {

  private final SellerRepository repository;

  public PageResult<Seller> execute(int page, int size) {
    log.info("Buscando vendedores (page={}, size={})", page, size);
    PageResult<Seller> result = repository.findPage(page, size);
    log.info(
        "Se encontraron {} vendedores (total: {})",
        result.content().size(),
        result.totalElements());
    return result;
  }
}
