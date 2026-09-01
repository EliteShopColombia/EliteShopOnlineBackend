package com.eliteshop.colombia.seller.infrastructure.mapper;

import com.eliteshop.colombia.seller.domain.model.*;
import com.eliteshop.colombia.seller.infrastructure.controller.dto.SellerBankInfoResponse;
import com.eliteshop.colombia.seller.infrastructure.controller.dto.SellerContactResponse;
import com.eliteshop.colombia.seller.infrastructure.controller.dto.SellerRequest;
import com.eliteshop.colombia.seller.infrastructure.controller.dto.SellerResponse;
import com.eliteshop.colombia.seller.infrastructure.persistence.SellerBankInfoEntity;
import com.eliteshop.colombia.seller.infrastructure.persistence.SellerContactEntity;
import com.eliteshop.colombia.seller.infrastructure.persistence.SellerEntity;
import java.sql.Timestamp;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class SellerMapper {

  public Seller toDomain(SellerEntity entity) {
    if (entity == null) return null;

    SellerContact contact = null;
    if (entity.getContact() != null) {
      contact =
          new SellerContact(
              new SellerEmail(entity.getContact().getEmail()),
              new SellerPhoneNumber(entity.getContact().getPhoneNumber()),
              new SellerTradeAddress(entity.getContact().getTradeAddress()),
              new SellerTradeDepartment(entity.getContact().getTradeDepartment()),
              new SellerTradeCity(entity.getContact().getTradeCity()));
    }

    SellerBankInfo bankInfo = null;
    if (entity.getBankInfo() != null) {
      bankInfo =
          new SellerBankInfo(
              new SellerBankName(entity.getBankInfo().getBankName()),
              SellerTypeBankAccount.valueOf(entity.getBankInfo().getTypeAccount()),
              new SellerNumberAccount(entity.getBankInfo().getNumberAccount()));
    }

    return new Seller(
        new SellerId(entity.getId()),
        SellerTypeTrade.valueOf(entity.getTypeTrade()),
        SellerTypeDni.valueOf(entity.getTypeDni()),
        new SellerDniNumber(entity.getDniNumber()),
        new SellerTradeName(entity.getTradeName()),
        new SellerFullname(entity.getFullname()),
        new SellerIsActive(entity.getIsActive()),
        new SellerIsVerified(entity.getIsVerified()),
        new SellerCreatedAt(entity.getCreatedAt()),
        entity.getUpdatedAt() != null ? new SellerUpdatedAt(entity.getUpdatedAt()) : null,
        entity.getProfileImage() != null ? new SellerProfileImage(entity.getProfileImage()) : null,
        contact,
        bankInfo);
  }

  public SellerEntity toEntity(Seller domain) {
    if (domain == null) return null;

    SellerEntity entity = new SellerEntity();
    entity.setId(domain.getId().getValue());
    entity.setTypeTrade(domain.getTypeTrade().name());
    entity.setTypeDni(domain.getTypeDni().name());
    entity.setDniNumber(domain.getDniNumber().getValue());
    entity.setTradeName(domain.getTradeName().getValue());
    entity.setFullname(domain.getFullname().getValue());
    entity.setIsActive(domain.getIsActive().getValue());
    entity.setIsVerified(domain.getIsVerified().getValue());
    entity.setCreatedAt(domain.getCreatedAt().getValue());
    if (domain.getUpdatedAt() != null) {
      entity.setUpdatedAt(domain.getUpdatedAt().getValue());
    }
    if (domain.getProfileImage() != null) {
      entity.setProfileImage(domain.getProfileImage().getValue());
    }

    if (domain.getContact() != null) {
      SellerContactEntity contactEntity = new SellerContactEntity();
      contactEntity.setSeller(entity);
      contactEntity.setEmail(domain.getContact().getEmail().getValue());
      contactEntity.setPhoneNumber(domain.getContact().getPhoneNumber().getValue());
      contactEntity.setTradeAddress(domain.getContact().getTradeAddress().getValue());
      contactEntity.setTradeDepartment(domain.getContact().getTradeDepartment().getValue());
      contactEntity.setTradeCity(domain.getContact().getTradeCity().getValue());
      entity.setContact(contactEntity);
    }

    if (domain.getBankInfo() != null) {
      SellerBankInfoEntity bankInfoEntity = new SellerBankInfoEntity();
      bankInfoEntity.setSeller(entity);
      bankInfoEntity.setBankName(domain.getBankInfo().getBankName().getValue());
      bankInfoEntity.setTypeAccount(domain.getBankInfo().getTypeBankAccount().name());
      bankInfoEntity.setNumberAccount(domain.getBankInfo().getNumberAccount().getValue());
      entity.setBankInfo(bankInfoEntity);
    }

    return entity;
  }

  public Seller toDomainFromRequest(SellerRequest request) {
    if (request == null) return null;

    SellerContact contact = null;
    if (request.getEmail() != null) {
      contact =
          new SellerContact(
              new SellerEmail(request.getEmail()),
              new SellerPhoneNumber(request.getPhoneNumber()),
              new SellerTradeAddress(request.getTradeAddress()),
              new SellerTradeDepartment(request.getTradeDepartment()),
              new SellerTradeCity(request.getTradeCity()));
    }

    SellerBankInfo bankInfo = null;
    if (request.getBankName() != null) {
      bankInfo =
          new SellerBankInfo(
              new SellerBankName(request.getBankName()),
              resolveTypeBankAccount(request.getTypeBankAccount()),
              new SellerNumberAccount(request.getNumberAccount()));
    }

    return new Seller(
        SellerId.generate(),
        resolveTypeTrade(request.getTypeTrade()),
        resolveTypeDni(request.getTypeDni()),
        new SellerDniNumber(request.getDniNumber()),
        new SellerTradeName(request.getTradeName()),
        new SellerFullname(request.getFullname()),
        new SellerIsActive(true),
        new SellerIsVerified(false),
        new SellerCreatedAt(Timestamp.from(Instant.now())),
        null,
        null,
        contact,
        bankInfo);
  }

  private SellerTypeTrade resolveTypeTrade(String input) {
    if (input == null) return null;
    for (SellerTypeTrade e : SellerTypeTrade.values()) {
      if (e.name().equalsIgnoreCase(input) || e.getValue().equalsIgnoreCase(input)) {
        return e;
      }
    }
    throw new IllegalArgumentException("Tipo de comercio no válido: " + input);
  }

  private SellerTypeDni resolveTypeDni(String input) {
    if (input == null) return null;
    for (SellerTypeDni e : SellerTypeDni.values()) {
      if (e.name().equalsIgnoreCase(input) || e.getValue().equalsIgnoreCase(input)) {
        return e;
      }
    }
    throw new IllegalArgumentException("Tipo de documento no válido: " + input);
  }

  private SellerTypeBankAccount resolveTypeBankAccount(String input) {
    if (input == null) return null;
    for (SellerTypeBankAccount e : SellerTypeBankAccount.values()) {
      if (e.name().equalsIgnoreCase(input) || e.getValue().equalsIgnoreCase(input)) {
        return e;
      }
    }
    throw new IllegalArgumentException("Tipo de cuenta bancaria no válido: " + input);
  }

  public SellerResponse toResponse(Seller domain) {
    if (domain == null) return null;

    SellerResponse response = new SellerResponse();
    response.setId(domain.getId().getValue());
    response.setTypeTrade(domain.getTypeTrade().name());
    response.setTypeDni(domain.getTypeDni().name());
    response.setDniNumber(domain.getDniNumber().getValue());
    response.setTradeName(domain.getTradeName().getValue());
    response.setFullname(domain.getFullname().getValue());
    response.setIsActive(domain.getIsActive().getValue());
    response.setIsVerified(domain.getIsVerified().getValue());
    response.setCreatedAt(domain.getCreatedAt().getValue());
    if (domain.getUpdatedAt() != null) {
      response.setUpdatedAt(domain.getUpdatedAt().getValue());
    }
    if (domain.getProfileImage() != null) {
      response.setProfileImage(domain.getProfileImage().getValue());
    }

    return response;
  }

  public SellerContactResponse toContactResponse(Seller domain) {
    if (domain == null || domain.getContact() == null) return null;

    SellerContactResponse response = new SellerContactResponse();
    response.setSellerId(domain.getId().getValue());
    response.setTradeName(domain.getTradeName().getValue());
    response.setFullname(domain.getFullname().getValue());
    response.setEmail(domain.getContact().getEmail().getValue());
    response.setPhoneNumber(domain.getContact().getPhoneNumber().getValue());
    response.setTradeAddress(domain.getContact().getTradeAddress().getValue());
    response.setTradeDepartment(domain.getContact().getTradeDepartment().getValue());
    response.setTradeCity(domain.getContact().getTradeCity().getValue());

    return response;
  }

  public SellerBankInfoResponse toBankInfoResponse(Seller domain) {
    if (domain == null || domain.getBankInfo() == null) return null;

    SellerBankInfoResponse response = new SellerBankInfoResponse();
    response.setSellerId(domain.getId().getValue());
    response.setTradeName(domain.getTradeName().getValue());
    response.setFullname(domain.getFullname().getValue());
    response.setBankName(domain.getBankInfo().getBankName().getValue());
    response.setTypeBankAccount(domain.getBankInfo().getTypeBankAccount().name());
    response.setNumberAccount(domain.getBankInfo().getNumberAccount().getValue());

    return response;
  }
}
