package com.eliteshop.colombia.customer.application;

import com.eliteshop.colombia.customer.domain.model.*;
import com.eliteshop.colombia.customer.domain.repository.CustomerRepository;
import com.eliteshop.colombia.customer.infrastructure.adapter.CustomerMinIOAdapter;
import java.io.InputStream;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CustomerAvatarUseCase {

  private final CustomerMinIOAdapter minIOAdapter;
  private final CustomerRepository customerRepository;

  public String upload(UUID customerId, String filename, InputStream stream) {
    log.info("Subiendo avatar para customerId={}", customerId);
    CustomerId id = new CustomerId(customerId);
    Customer customer = customerRepository.findById(id).orElseThrow();

    if (customer.getProfileImage() != null) {
      String oldKey = customer.getProfileImage().getValue();
      log.info("Eliminando avatar anterior para customerId={}", customerId);
      minIOAdapter.deleteAvatar(oldKey);
    }

    String objectKey =
        minIOAdapter.uploadAvatar(
            customerId.toString(), UUID.randomUUID() + "_" + filename, stream);

    var updated =
        new Customer(
            customer.getId(),
            customer.getFirstName(),
            customer.getLastName(),
            customer.getEmail(),
            customer.getPhoneNumber(),
            customer.getPassword(),
            new CustomerProfileImage(objectKey),
            customer.getRole(),
            customer.getCreatedAt(),
            customer.getUpdatedAt(),
            customer.getInfo());
    customerRepository.update(updated);

    log.info("Avatar subido exitosamente para customerId={}: {}", customerId, objectKey);
    return objectKey;
  }

  public void delete(UUID customerId) {
    log.info("Eliminando avatar para customerId={}", customerId);
    CustomerId id = new CustomerId(customerId);
    Customer customer = customerRepository.findById(id).orElseThrow();

    if (customer.getProfileImage() != null) {
      minIOAdapter.deleteAvatar(customer.getProfileImage().getValue());
      var updated =
          new Customer(
              customer.getId(),
              customer.getFirstName(),
              customer.getLastName(),
              customer.getEmail(),
              customer.getPhoneNumber(),
              customer.getPassword(),
              null,
              customer.getRole(),
              customer.getCreatedAt(),
              customer.getUpdatedAt(),
              customer.getInfo());
      customerRepository.update(updated);
      log.info("Avatar eliminado para customerId={}", customerId);
    }
  }

  public InputStream download(UUID customerId) {
    log.info("Descargando avatar para customerId={}", customerId);
    CustomerId id = new CustomerId(customerId);
    Customer customer = customerRepository.findById(id).orElseThrow();

    if (customer.getProfileImage() == null) {
      throw new com.eliteshop.colombia.customer.domain.exception.AvatarNotFoundException(
          "Cliente no tiene avatar");
    }

    return minIOAdapter.downloadAvatar(customer.getProfileImage().getValue());
  }
}
