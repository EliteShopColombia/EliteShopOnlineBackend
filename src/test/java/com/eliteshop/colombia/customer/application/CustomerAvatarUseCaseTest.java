package com.eliteshop.colombia.customer.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.eliteshop.colombia.customer.domain.model.*;
import com.eliteshop.colombia.customer.domain.repository.CustomerRepository;
import com.eliteshop.colombia.customer.infrastructure.adapter.CustomerMinIOAdapter;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CustomerAvatarUseCaseTest {

  private CustomerMinIOAdapter minIOAdapter;
  private CustomerRepository customerRepository;
  private CustomerAvatarUseCase useCase;

  @BeforeEach
  void setUp() {
    minIOAdapter = mock(CustomerMinIOAdapter.class);
    customerRepository = mock(CustomerRepository.class);
    useCase = new CustomerAvatarUseCase(minIOAdapter, customerRepository);
  }

  @Test
  void shouldUploadNewAvatarWhenNoExisting() {
    UUID customerId = UUID.randomUUID();
    Customer customer = buildCustomer(customerId, null);
    InputStream stream = new ByteArrayInputStream("fake-image".getBytes());

    when(customerRepository.findById(any(CustomerId.class))).thenReturn(Optional.of(customer));
    when(minIOAdapter.uploadAvatar(eq(customerId.toString()), anyString(), any(InputStream.class)))
        .thenReturn("avatars/customers/" + customerId + "/file.jpg");

    String result = useCase.upload(customerId, "file.jpg", stream);

    assertThat(result).startsWith("avatars/customers/" + customerId);
    verify(minIOAdapter, never()).deleteAvatar(anyString());
    verify(customerRepository).update(any(Customer.class));
  }

  @Test
  void shouldReplaceExistingAvatar() {
    UUID customerId = UUID.randomUUID();
    String oldKey = "avatars/customers/" + customerId + "/old.jpg";
    Customer customer = buildCustomer(customerId, oldKey);
    InputStream stream = new ByteArrayInputStream("fake-image".getBytes());

    when(customerRepository.findById(any(CustomerId.class))).thenReturn(Optional.of(customer));
    when(minIOAdapter.uploadAvatar(eq(customerId.toString()), anyString(), any(InputStream.class)))
        .thenReturn("avatars/customers/" + customerId + "/new.jpg");

    String result = useCase.upload(customerId, "new.jpg", stream);

    verify(minIOAdapter).deleteAvatar(oldKey);
    assertThat(result).startsWith("avatars/customers/" + customerId);
  }

  @Test
  void shouldDeleteExistingAvatar() {
    UUID customerId = UUID.randomUUID();
    String oldKey = "avatars/customers/" + customerId + "/photo.jpg";
    Customer customer = buildCustomer(customerId, oldKey);

    when(customerRepository.findById(any(CustomerId.class))).thenReturn(Optional.of(customer));

    useCase.delete(customerId);

    verify(minIOAdapter).deleteAvatar(oldKey);
    verify(customerRepository).update(any(Customer.class));
  }

  @Test
  void shouldDoNothingWhenDeletingNonExistentAvatar() {
    UUID customerId = UUID.randomUUID();
    Customer customer = buildCustomer(customerId, null);

    when(customerRepository.findById(any(CustomerId.class))).thenReturn(Optional.of(customer));

    useCase.delete(customerId);

    verify(minIOAdapter, never()).deleteAvatar(anyString());
    verify(customerRepository, never()).update(any(Customer.class));
  }

  @Test
  void shouldDownloadExistingAvatar() {
    UUID customerId = UUID.randomUUID();
    String key = "avatars/customers/" + customerId + "/photo.jpg";
    Customer customer = buildCustomer(customerId, key);
    InputStream expectedStream = new ByteArrayInputStream("image-data".getBytes());

    when(customerRepository.findById(any(CustomerId.class))).thenReturn(Optional.of(customer));
    when(minIOAdapter.downloadAvatar(key)).thenReturn(expectedStream);

    InputStream result = useCase.download(customerId);

    assertThat(result).isNotNull();
    verify(minIOAdapter).downloadAvatar(key);
  }

  @Test
  void shouldThrowWhenDownloadingNonExistentAvatar() {
    UUID customerId = UUID.randomUUID();
    Customer customer = buildCustomer(customerId, null);

    when(customerRepository.findById(any(CustomerId.class))).thenReturn(Optional.of(customer));

    assertThatThrownBy(() -> useCase.download(customerId))
        .isInstanceOf(
            com.eliteshop.colombia.customer.domain.exception.AvatarNotFoundException.class)
        .hasMessage("Cliente no tiene avatar");
  }

  private Customer buildCustomer(UUID id, String profileImageKey) {
    return new Customer(
        new CustomerId(id),
        new CustomerFirstName("Juan"),
        new CustomerLastName("Perez"),
        new CustomerEmail("juan@test.com"),
        new CustomerPhoneNumber("3001234567"),
        new CustomerPassword("hashed"),
        profileImageKey != null ? new CustomerProfileImage(profileImageKey) : null,
        new CustomerRole("customer"),
        new CustomerCreatedAt(Timestamp.from(Instant.now())),
        null,
        null);
  }
}
