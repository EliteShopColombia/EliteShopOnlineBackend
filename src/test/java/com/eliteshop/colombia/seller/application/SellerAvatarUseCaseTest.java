package com.eliteshop.colombia.seller.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.eliteshop.colombia.seller.domain.model.*;
import com.eliteshop.colombia.seller.domain.repository.SellerRepository;
import com.eliteshop.colombia.seller.infrastructure.adapter.SellerMinIOAdapter;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SellerAvatarUseCaseTest {

  private SellerMinIOAdapter minIOAdapter;
  private SellerRepository sellerRepository;
  private SellerAvatarUseCase useCase;

  @BeforeEach
  void setUp() {
    minIOAdapter = mock(SellerMinIOAdapter.class);
    sellerRepository = mock(SellerRepository.class);
    useCase = new SellerAvatarUseCase(minIOAdapter, sellerRepository);
  }

  @Test
  void shouldUploadNewAvatarWhenNoExisting() {
    UUID sellerId = UUID.randomUUID();
    Seller seller = buildSeller(sellerId, null);
    InputStream stream = new ByteArrayInputStream("fake-image".getBytes());

    when(sellerRepository.findById(any(SellerId.class))).thenReturn(Optional.of(seller));
    when(minIOAdapter.uploadAvatar(eq(sellerId.toString()), anyString(), any(InputStream.class)))
        .thenReturn("avatars/sellers/" + sellerId + "/file.jpg");

    String result = useCase.upload(sellerId, "file.jpg", stream);

    assertThat(result).startsWith("avatars/sellers/" + sellerId);
    verify(minIOAdapter, never()).deleteAvatar(anyString());
    verify(sellerRepository).update(any(Seller.class));
  }

  @Test
  void shouldReplaceExistingAvatar() {
    UUID sellerId = UUID.randomUUID();
    String oldKey = "avatars/sellers/" + sellerId + "/old.jpg";
    Seller seller = buildSeller(sellerId, oldKey);
    InputStream stream = new ByteArrayInputStream("fake-image".getBytes());

    when(sellerRepository.findById(any(SellerId.class))).thenReturn(Optional.of(seller));
    when(minIOAdapter.uploadAvatar(eq(sellerId.toString()), anyString(), any(InputStream.class)))
        .thenReturn("avatars/sellers/" + sellerId + "/new.jpg");

    String result = useCase.upload(sellerId, "new.jpg", stream);

    verify(minIOAdapter).deleteAvatar(oldKey);
    assertThat(result).startsWith("avatars/sellers/" + sellerId);
  }

  @Test
  void shouldDeleteExistingAvatar() {
    UUID sellerId = UUID.randomUUID();
    String oldKey = "avatars/sellers/" + sellerId + "/photo.jpg";
    Seller seller = buildSeller(sellerId, oldKey);

    when(sellerRepository.findById(any(SellerId.class))).thenReturn(Optional.of(seller));

    useCase.delete(sellerId);

    verify(minIOAdapter).deleteAvatar(oldKey);
    verify(sellerRepository).update(any(Seller.class));
  }

  @Test
  void shouldDoNothingWhenDeletingNonExistentAvatar() {
    UUID sellerId = UUID.randomUUID();
    Seller seller = buildSeller(sellerId, null);

    when(sellerRepository.findById(any(SellerId.class))).thenReturn(Optional.of(seller));

    useCase.delete(sellerId);

    verify(minIOAdapter, never()).deleteAvatar(anyString());
    verify(sellerRepository, never()).update(any(Seller.class));
  }

  @Test
  void shouldDownloadExistingAvatar() {
    UUID sellerId = UUID.randomUUID();
    String key = "avatars/sellers/" + sellerId + "/photo.jpg";
    Seller seller = buildSeller(sellerId, key);
    InputStream expectedStream = new ByteArrayInputStream("image-data".getBytes());

    when(sellerRepository.findById(any(SellerId.class))).thenReturn(Optional.of(seller));
    when(minIOAdapter.downloadAvatar(key)).thenReturn(expectedStream);

    InputStream result = useCase.download(sellerId);

    assertThat(result).isNotNull();
    verify(minIOAdapter).downloadAvatar(key);
  }

  @Test
  void shouldThrowWhenDownloadingNonExistentAvatar() {
    UUID sellerId = UUID.randomUUID();
    Seller seller = buildSeller(sellerId, null);

    when(sellerRepository.findById(any(SellerId.class))).thenReturn(Optional.of(seller));

    assertThatThrownBy(() -> useCase.download(sellerId))
        .isInstanceOf(com.eliteshop.colombia.seller.domain.exception.SellerNotFoundException.class)
        .hasMessage("Vendedor no tiene avatar");
  }

  private Seller buildSeller(UUID id, String profileImageKey) {
    SellerContact contact =
        new SellerContact(
            new SellerEmail("seller@test.com"),
            new SellerPhoneNumber("3001234567"),
            new SellerTradeAddress("Calle 1"),
            new SellerTradeDepartment("Bogota"),
            new SellerTradeCity("Bogota"));

    return new Seller(
        new SellerId(id),
        SellerTypeTrade.NATURAL,
        SellerTypeDni.CC,
        new SellerDniNumber("12345678"),
        new SellerTradeName("Mi Tienda"),
        new SellerFullname("Juan Vendedor"),
        new SellerIsActive(true),
        new SellerIsVerified(false),
        new SellerCreatedAt(Timestamp.from(Instant.now())),
        null,
        profileImageKey != null ? new SellerProfileImage(profileImageKey) : null,
        contact,
        null);
  }
}
