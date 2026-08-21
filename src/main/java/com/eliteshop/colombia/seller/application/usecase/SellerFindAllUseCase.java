package com.eliteshop.colombia.seller.application.usecase;

import com.eliteshop.colombia.seller.domain.model.Seller;
import com.eliteshop.colombia.seller.domain.repository.SellerRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SellerFindAllUseCase {

  private final SellerRepository repository;

  public List<Seller> execute() {
    log.info("Buscando todos los vendedores");
    List<Seller> sellers = repository.findAll();
    log.info("Se encontraron {} vendedores", sellers.size());
    return sellers;
  }
}
