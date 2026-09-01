package com.eliteshop.colombia.payment.infrastructure.adapter.paymentmethod;

import com.eliteshop.colombia.payment.domain.model.paymentmethod.*;
import com.eliteshop.colombia.payment.domain.port.CustomerPaymentMethodRepository;
import com.eliteshop.colombia.payment.infrastructure.persistence.paymentmethod.CustomerPaymentMethodEntity;
import com.eliteshop.colombia.payment.infrastructure.persistence.paymentmethod.CustomerPaymentMethodJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CustomerPaymentMethodPostgresAdapter implements CustomerPaymentMethodRepository {

  private final CustomerPaymentMethodJpaRepository jpaRepository;

  @Override
  @Transactional
  public CustomerPaymentMethod save(CustomerPaymentMethod paymentMethod) {
    if (paymentMethod.isDefault()) {
      clearDefault(paymentMethod.getCustomerId().getValue());
    }
    CustomerPaymentMethodEntity entity = toEntity(paymentMethod);
    CustomerPaymentMethodEntity saved = jpaRepository.save(entity);
    return toDomain(saved);
  }

  @Override
  @Transactional(readOnly = true)
  public List<CustomerPaymentMethod> findByCustomerId(UUID customerId) {
    return jpaRepository.findByCustomerId(customerId).stream()
        .map(this::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<CustomerPaymentMethod> findById(UUID id) {
    return jpaRepository.findById(id).map(this::toDomain);
  }

  @Override
  @Transactional
  public void deleteById(UUID id) {
    jpaRepository.deleteById(id);
  }

  @Override
  @Transactional
  public void clearDefault(UUID customerId) {
    jpaRepository
        .findFirstByCustomerIdAndIsDefaultTrue(customerId)
        .ifPresent(
            entity -> {
              entity.setDefault(false);
              jpaRepository.save(entity);
            });
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<CustomerPaymentMethod> findByDefault(UUID customerId) {
    return jpaRepository.findFirstByCustomerIdAndIsDefaultTrue(customerId).map(this::toDomain);
  }

  private CustomerPaymentMethod toDomain(CustomerPaymentMethodEntity entity) {
    if (entity == null) return null;
    return new CustomerPaymentMethod(
        new CustomerPaymentMethodId(entity.getId()),
        new CustomerPaymentMethodCustomerId(entity.getCustomerId()),
        new CustomerPaymentMethodEpaycoToken(entity.getEpaycoToken()),
        new CustomerPaymentMethodEpaycoCustomerId(entity.getEpaycoCustomerId()),
        new CustomerPaymentMethodLast4(entity.getLast4()),
        new CustomerPaymentMethodBrand(entity.getBrand()),
        new CustomerPaymentMethodExpiryMonth(entity.getExpiryMonth()),
        new CustomerPaymentMethodExpiryYear(entity.getExpiryYear()),
        new CustomerPaymentMethodDocType(entity.getDocType()),
        new CustomerPaymentMethodDocNumber(entity.getDocNumber()),
        entity.isDefault(),
        entity.getCreatedAt());
  }

  private CustomerPaymentMethodEntity toEntity(CustomerPaymentMethod domain) {
    if (domain == null) return null;
    CustomerPaymentMethodEntity entity = new CustomerPaymentMethodEntity();
    entity.setId(domain.getId().getValue());
    entity.setCustomerId(domain.getCustomerId().getValue());
    entity.setEpaycoToken(domain.getEpaycoToken().getValue());
    entity.setEpaycoCustomerId(domain.getEpaycoCustomerId().getValue());
    entity.setLast4(domain.getLast4().getValue());
    entity.setBrand(domain.getBrand().getValue());
    entity.setExpiryMonth(domain.getExpiryMonth().getValue());
    entity.setExpiryYear(domain.getExpiryYear().getValue());
    entity.setDocType(domain.getDocType() != null ? domain.getDocType().getValue() : null);
    entity.setDocNumber(domain.getDocNumber() != null ? domain.getDocNumber().getValue() : null);
    entity.setDefault(domain.isDefault());
    entity.setCreatedAt(domain.getCreatedAt());
    return entity;
  }
}
