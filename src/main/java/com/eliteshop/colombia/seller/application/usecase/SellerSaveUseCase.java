package com.eliteshop.colombia.seller.application.usecase;

import com.eliteshop.colombia.seller.domain.event.SellerCreatedEvent;
import com.eliteshop.colombia.seller.domain.exception.SellerAlreadyExistsException;
import com.eliteshop.colombia.seller.domain.model.Seller;
import com.eliteshop.colombia.seller.domain.repository.SellerRepository;
import com.eliteshop.colombia.shared.domain.LocationValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class SellerSaveUseCase {

  private final SellerRepository repository;
  private final ApplicationEventPublisher eventPublisher;
  private final LocationValidationService locationValidationService;

  @Transactional
  public void execute(Seller seller) {
    log.info("Guardando vendedor con dni={}", seller.getDniNumber());

    // Validate location before persisting
    if (seller.getContact() != null) {
      String department = seller.getContact().getTradeDepartment().getValue();
      String city = seller.getContact().getTradeCity().getValue();
      locationValidationService.validateLocation(department, city);
    }

    repository
        .findByDniNumber(seller.getDniNumber())
        .ifPresent(
            existingSeller -> {
              log.error("Ya existe un vendedor con el dni={}", seller.getDniNumber());
              throw new SellerAlreadyExistsException(
                  "Ya existe un vendedor con el documento " + seller.getDniNumber().getValue());
            });

    repository.save(seller);

    eventPublisher.publishEvent(
        SellerCreatedEvent.of(
            seller.getId().getValue(), seller.getContact().getEmail().getValue()));
    log.info("Vendedor guardado exitosamente con id={}", seller.getId());
  }
}
