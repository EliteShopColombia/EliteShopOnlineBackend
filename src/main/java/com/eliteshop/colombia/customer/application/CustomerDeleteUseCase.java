package com.eliteshop.colombia.customer.application;

import com.eliteshop.colombia.customer.domain.exception.CustomerNotFoundException;
import com.eliteshop.colombia.customer.domain.model.CustomerId;
import com.eliteshop.colombia.customer.domain.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CustomerDeleteUseCase {

  private final CustomerRepository repository;

  public void execute(CustomerId id) {
    log.info("Iniciando eliminación de cliente con id: {}", id);

    if (!repository.findById(id).isPresent()) {
      log.error("Cliente no encontrado con id: {}", id);
      throw new CustomerNotFoundException("Customer no encontrado con id: " + id);
    }

    this.repository.delete(id);
    log.info("Cliente eliminado exitosamente con id: {}", id);
  }
}
