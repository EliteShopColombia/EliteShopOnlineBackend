package com.eliteshop.colombia.seller.application;

import com.eliteshop.colombia.seller.domain.model.*;
import com.eliteshop.colombia.seller.domain.repository.SellerRepository;
import com.eliteshop.colombia.seller.infrastructure.adapter.SellerMinIOAdapter;
import java.io.InputStream;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SellerAvatarUseCase {

  private final SellerMinIOAdapter minIOAdapter;
  private final SellerRepository sellerRepository;

  public String upload(UUID sellerId, String filename, InputStream stream) {
    log.info("Subiendo avatar para sellerId={}", sellerId);
    SellerId id = new SellerId(sellerId);
    Seller seller = sellerRepository.findById(id).orElseThrow();

    if (seller.getProfileImage() != null) {
      String oldKey = seller.getProfileImage().getValue();
      log.info("Eliminando avatar anterior para sellerId={}", sellerId);
      minIOAdapter.deleteAvatar(oldKey);
    }

    String objectKey =
        minIOAdapter.uploadAvatar(sellerId.toString(), UUID.randomUUID() + "_" + filename, stream);

    var updated =
        new Seller(
            seller.getId(),
            seller.getTypeTrade(),
            seller.getTypeDni(),
            seller.getDniNumber(),
            seller.getTradeName(),
            seller.getFullname(),
            seller.getIsActive(),
            seller.getIsVerified(),
            seller.getCreatedAt(),
            seller.getUpdatedAt(),
            new SellerProfileImage(objectKey),
            seller.getContact(),
            seller.getBankInfo());
    sellerRepository.update(updated);

    log.info("Avatar subido exitosamente para sellerId={}: {}", sellerId, objectKey);
    return objectKey;
  }

  public void delete(UUID sellerId) {
    log.info("Eliminando avatar para sellerId={}", sellerId);
    SellerId id = new SellerId(sellerId);
    Seller seller = sellerRepository.findById(id).orElseThrow();

    if (seller.getProfileImage() != null) {
      minIOAdapter.deleteAvatar(seller.getProfileImage().getValue());
      var updated =
          new Seller(
              seller.getId(),
              seller.getTypeTrade(),
              seller.getTypeDni(),
              seller.getDniNumber(),
              seller.getTradeName(),
              seller.getFullname(),
              seller.getIsActive(),
              seller.getIsVerified(),
              seller.getCreatedAt(),
              seller.getUpdatedAt(),
              null,
              seller.getContact(),
              seller.getBankInfo());
      sellerRepository.update(updated);
      log.info("Avatar eliminado para sellerId={}", sellerId);
    }
  }

  public InputStream download(UUID sellerId) {
    log.info("Descargando avatar para sellerId={}", sellerId);
    SellerId id = new SellerId(sellerId);
    Seller seller = sellerRepository.findById(id).orElseThrow();

    if (seller.getProfileImage() == null) {
      throw new com.eliteshop.colombia.seller.domain.exception.SellerNotFoundException(
          "Vendedor no tiene avatar");
    }

    return minIOAdapter.downloadAvatar(seller.getProfileImage().getValue());
  }
}
