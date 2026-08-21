package com.eliteshop.colombia.seller.infrastructure.adapter;

import com.eliteshop.colombia.seller.domain.model.Seller;
import com.eliteshop.colombia.seller.domain.model.SellerDniNumber;
import com.eliteshop.colombia.seller.domain.model.SellerId;
import com.eliteshop.colombia.seller.domain.repository.SellerRepository;
import com.eliteshop.colombia.seller.infrastructure.mapper.SellerMapper;
import com.eliteshop.colombia.seller.infrastructure.persistence.SellerEntity;
import com.eliteshop.colombia.seller.infrastructure.persistence.SellerJpaRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SellerPostgresAdapter implements SellerRepository {

  private final SellerJpaRepository jpaRepository;
  private final SellerMapper mapper;

  @Override
  public Seller save(Seller seller) {
    SellerEntity entity = mapper.toEntity(seller);
    SellerEntity savedEntity = jpaRepository.save(entity);
    return mapper.toDomain(savedEntity);
  }

  @Override
  public void update(Seller seller) {
    SellerEntity existingEntity = jpaRepository.findById(seller.getId().getValue()).orElseThrow();

    existingEntity.setTypeTrade(seller.getTypeTrade().name());
    existingEntity.setTypeDni(seller.getTypeDni().name());
    existingEntity.setDniNumber(seller.getDniNumber().getValue());
    existingEntity.setTradeName(seller.getTradeName().getValue());
    existingEntity.setFullname(seller.getFullname().getValue());
    existingEntity.setIsActive(seller.getIsActive().getValue());
    existingEntity.setUpdatedAt(Timestamp.from(Instant.now()));

    if (seller.getContact() != null && existingEntity.getContact() != null) {
      existingEntity.getContact().setEmail(seller.getContact().getEmail().getValue());
      existingEntity.getContact().setPhoneNumber(seller.getContact().getPhoneNumber().getValue());
      existingEntity.getContact().setTradeAddress(seller.getContact().getTradeAddress().getValue());
      existingEntity
          .getContact()
          .setTradeDepartment(seller.getContact().getTradeDepartment().getValue());
      existingEntity.getContact().setTradeCity(seller.getContact().getTradeCity().getValue());
    }

    if (seller.getBankInfo() != null && existingEntity.getBankInfo() != null) {
      existingEntity.getBankInfo().setBankName(seller.getBankInfo().getBankName().getValue());
      existingEntity.getBankInfo().setTypeAccount(seller.getBankInfo().getTypeBankAccount().name());
      existingEntity
          .getBankInfo()
          .setNumberAccount(seller.getBankInfo().getNumberAccount().getValue());
    }

    jpaRepository.save(existingEntity);
  }

  @Override
  public void delete(SellerId id) {
    jpaRepository.deleteById(id.getValue());
  }

  @Override
  public List<Seller> findAll() {
    return jpaRepository.findAll().stream().map(mapper::toDomain).collect(Collectors.toList());
  }

  @Override
  public Optional<Seller> findById(SellerId id) {
    return jpaRepository.findById(id.getValue()).map(mapper::toDomain);
  }

  @Override
  public Optional<Seller> findByDniNumber(SellerDniNumber dniNumber) {
    return jpaRepository.findByDniNumber(dniNumber.getValue()).map(mapper::toDomain);
  }

  @Override
  public Optional<Seller> findByEmail(String email) {
    return jpaRepository.findByContactEmail(email).map(mapper::toDomain);
  }
}
