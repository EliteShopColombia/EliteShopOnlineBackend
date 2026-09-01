package com.eliteshop.colombia.seller.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.eliteshop.colombia.seller.application.usecase.*;
import com.eliteshop.colombia.seller.domain.exception.SellerNotFoundException;
import com.eliteshop.colombia.seller.domain.model.*;
import com.eliteshop.colombia.seller.domain.repository.SellerRepository;
import com.eliteshop.colombia.shared.domain.PageResult;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SellerUseCasesTest {

  @Mock private SellerRepository sellerRepository;

  private SellerFindByIdUseCase findByIdUseCase;
  private SellerDeleteUseCase deleteUseCase;
  private SellerUpdateUseCase updateUseCase;
  private SellerFindAllUseCase findAllUseCase;
  private SellerFindByDniUseCase findByDniUseCase;

  private UUID sellerId;

  @BeforeEach
  void setUp() {
    findByIdUseCase = new SellerFindByIdUseCase(sellerRepository);
    deleteUseCase = new SellerDeleteUseCase(sellerRepository);
    updateUseCase = new SellerUpdateUseCase(sellerRepository);
    findAllUseCase = new SellerFindAllUseCase(sellerRepository);
    findByDniUseCase = new SellerFindByDniUseCase(sellerRepository);
    sellerId = UUID.randomUUID();
  }

  // ==================== SellerFindByIdUseCase ====================

  @Test
  void findById_shouldReturnEmptyWhenIdIsNull() {
    Optional<Seller> result = findByIdUseCase.execute(null);
    assertThat(result).isEmpty();
    verifyNoInteractions(sellerRepository);
  }

  @Test
  void findById_shouldReturnSellerWhenExists() {
    Seller seller = buildSeller(sellerId);
    when(sellerRepository.findById(any(SellerId.class))).thenReturn(Optional.of(seller));

    Optional<Seller> result = findByIdUseCase.execute(new SellerId(sellerId));

    assertThat(result).isPresent();
    assertThat(result.get().getId().getValue()).isEqualTo(sellerId);
  }

  @Test
  void findById_shouldReturnEmptyWhenNotFound() {
    when(sellerRepository.findById(any(SellerId.class))).thenReturn(Optional.empty());

    Optional<Seller> result = findByIdUseCase.execute(new SellerId(sellerId));

    assertThat(result).isEmpty();
  }

  // ==================== SellerDeleteUseCase ====================

  @Test
  void delete_shouldThrowWhenSellerNotFound() {
    when(sellerRepository.findById(any(SellerId.class))).thenReturn(Optional.empty());

    assertThatThrownBy(() -> deleteUseCase.execute(new SellerId(sellerId)))
        .isInstanceOf(SellerNotFoundException.class);
  }

  @Test
  void delete_shouldDeleteWhenSellerExists() {
    Seller seller = buildSeller(sellerId);
    when(sellerRepository.findById(any(SellerId.class))).thenReturn(Optional.of(seller));

    deleteUseCase.execute(new SellerId(sellerId));

    verify(sellerRepository).delete(any(SellerId.class));
  }

  // ==================== SellerUpdateUseCase ====================

  @Test
  void update_shouldThrowWhenSellerNotFound() {
    Seller seller = buildSeller(sellerId);
    when(sellerRepository.findById(any(SellerId.class))).thenReturn(Optional.empty());

    assertThatThrownBy(() -> updateUseCase.execute(seller))
        .isInstanceOf(SellerNotFoundException.class);
  }

  @Test
  void update_shouldUpdateWhenSellerExists() {
    Seller seller = buildSeller(sellerId);
    when(sellerRepository.findById(any(SellerId.class))).thenReturn(Optional.of(seller));

    updateUseCase.execute(seller);

    verify(sellerRepository).update(any(Seller.class));
  }

  // ==================== SellerFindAllUseCase ====================

  @Test
  void findAll_shouldReturnPageResult() {
    Seller seller = buildSeller(sellerId);
    PageResult<Seller> pageResult = new PageResult<>(List.of(seller), 0, 10, 1L, 1);
    when(sellerRepository.findPage(0, 10)).thenReturn(pageResult);

    PageResult<Seller> result = findAllUseCase.execute(0, 10);

    assertThat(result.content()).hasSize(1);
    assertThat(result.totalElements()).isEqualTo(1L);
  }

  // ==================== SellerFindByDniUseCase ====================

  @Test
  void findByDni_shouldReturnEmptyWhenDniIsNull() {
    Optional<Seller> result = findByDniUseCase.execute(null);
    assertThat(result).isEmpty();
    verifyNoInteractions(sellerRepository);
  }

  @Test
  void findByDni_shouldReturnSellerWhenExists() {
    Seller seller = buildSeller(sellerId);
    SellerDniNumber dni = new SellerDniNumber("12345678");
    when(sellerRepository.findByDniNumber(any(SellerDniNumber.class)))
        .thenReturn(Optional.of(seller));

    Optional<Seller> result = findByDniUseCase.execute(dni);

    assertThat(result).isPresent();
  }

  @Test
  void findByDni_shouldReturnEmptyWhenNotFound() {
    SellerDniNumber dni = new SellerDniNumber("12345678");
    when(sellerRepository.findByDniNumber(any(SellerDniNumber.class))).thenReturn(Optional.empty());

    Optional<Seller> result = findByDniUseCase.execute(dni);

    assertThat(result).isEmpty();
  }

  // ==================== Helpers ====================

  private Seller buildSeller(UUID id) {
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
        null,
        contact,
        null);
  }
}
