package com.eliteshop.colombia.product.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.eliteshop.colombia.product.application.usecase.*;
import com.eliteshop.colombia.product.domain.exception.ProductNotFoundException;
import com.eliteshop.colombia.product.domain.model.*;
import com.eliteshop.colombia.product.domain.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductUseCasesTest {

  @Mock private ProductRepository productRepository;

  private ProductFindByIdUseCase findByIdUseCase;
  private ProductDeleteUseCase deleteUseCase;
  private ProductSaveUseCase saveUseCase;

  private UUID productId;

  @BeforeEach
  void setUp() {
    findByIdUseCase = new ProductFindByIdUseCase(productRepository);
    deleteUseCase = new ProductDeleteUseCase(productRepository);
    saveUseCase = new ProductSaveUseCase(productRepository);
    productId = UUID.randomUUID();
  }

  // ==================== ProductFindByIdUseCase ====================

  @Test
  void findById_shouldReturnEmptyWhenIdIsNull() {
    Optional<Product> result = findByIdUseCase.execute(null);
    assertThat(result).isEmpty();
    verifyNoInteractions(productRepository);
  }

  @Test
  void findById_shouldReturnProductWhenExists() {
    Product product = buildProduct();
    when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.of(product));

    Optional<Product> result = findByIdUseCase.execute(new ProductId(productId));

    assertThat(result).isPresent();
    assertThat(result.get().getId().getValue()).isEqualTo(productId);
  }

  @Test
  void findById_shouldReturnEmptyWhenNotFound() {
    when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.empty());

    Optional<Product> result = findByIdUseCase.execute(new ProductId(productId));

    assertThat(result).isEmpty();
  }

  // ==================== ProductDeleteUseCase ====================

  @Test
  void delete_shouldThrowWhenProductNotFound() {
    when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.empty());

    assertThatThrownBy(() -> deleteUseCase.execute(new ProductId(productId)))
        .isInstanceOf(ProductNotFoundException.class);
  }

  @Test
  void delete_shouldDeleteWhenProductExists() {
    Product product = buildProduct();
    when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.of(product));

    deleteUseCase.execute(new ProductId(productId));

    verify(productRepository).delete(any(ProductId.class));
  }

  // ==================== ProductSaveUseCase ====================

  @Test
  void save_shouldSaveWhenNameIsUnique() {
    Product product = buildProduct();
    when(productRepository.findByName(any(ProductName.class))).thenReturn(Optional.empty());

    saveUseCase.execute(product);

    verify(productRepository).save(any(Product.class));
  }

  @Test
  void save_shouldThrowWhenNameAlreadyExists() {
    Product product = buildProduct();
    Product existingProduct = buildProduct();
    when(productRepository.findByName(any(ProductName.class)))
        .thenReturn(Optional.of(existingProduct));

    assertThatThrownBy(() -> saveUseCase.execute(product))
        .isInstanceOf(
            com.eliteshop.colombia.product.domain.exception.ProductAlreadyExistsException.class);
  }

  // ==================== Helpers ====================

  private Product buildProduct() {
    return new Product(
        new ProductId(productId),
        new ProductSellerId(UUID.randomUUID()),
        new ProductName("Test Product"),
        new ProductPrice(BigDecimal.valueOf(50.00)),
        new ProductStock(10),
        java.util.List.of());
  }
}
