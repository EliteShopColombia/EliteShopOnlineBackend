package com.eliteshop.colombia.customer.application;

import com.eliteshop.colombia.customer.domain.model.Customer;
import com.eliteshop.colombia.customer.domain.repository.CustomerRepository;
import com.eliteshop.colombia.shared.domain.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CustomerFindAllUseCase {

  private final CustomerRepository repository;

  public PageResult<Customer> execute(int page, int size) {
    log.info("Buscando clientes (page={}, size={})", page, size);
    PageResult<Customer> result = repository.findPage(page, size);
    log.info(
        "Se encontraron {} clientes (total: {})", result.content().size(), result.totalElements());
    return result;
  }
}
