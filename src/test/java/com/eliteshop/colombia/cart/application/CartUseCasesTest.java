package com.eliteshop.colombia.cart.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.eliteshop.colombia.cart.domain.exception.CartNotFoundException;
import com.eliteshop.colombia.cart.domain.model.*;
import com.eliteshop.colombia.cart.domain.repository.CartRepository;
import com.eliteshop.colombia.product.domain.exception.ProductNotFoundException;
import com.eliteshop.colombia.product.domain.model.*;
import com.eliteshop.colombia.product.domain.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartUseCasesTest {

  @Mock private CartRepository cartRepository;
  @Mock private ProductRepository productRepository;

  @InjectMocks private AddToCartUseCase addToCartUseCase;
  @InjectMocks private UpdateCartItemUseCase updateCartItemUseCase;
  @InjectMocks private RemoveFromCartUseCase removeFromCartUseCase;
  @InjectMocks private GetCartUseCase getCartUseCase;
  @InjectMocks private ClearCartUseCase clearCartUseCase;

  private UUID customerId;
  private UUID productId;

  @BeforeEach
  void setUp() {
    customerId = UUID.randomUUID();
    productId = UUID.randomUUID();
  }

  // ==================== AddToCartUseCase ====================

  @Test
  void addToCart_shouldCreateNewCartWhenNoExistingCart() {
    Product product = buildProduct();
    when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.of(product));
    when(cartRepository.findByCustomerId(customerId)).thenReturn(Optional.empty());

    Cart newCart = Cart.create(customerId);
    when(cartRepository.save(any(Cart.class))).thenReturn(newCart);

    Cart result = addToCartUseCase.execute(customerId, productId, 2);

    assertThat(result).isNotNull();
    verify(cartRepository).save(any(Cart.class));
  }

  @Test
  void addToCart_shouldThrowWhenProductNotFound() {
    when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.empty());

    assertThatThrownBy(() -> addToCartUseCase.execute(customerId, productId, 1))
        .isInstanceOf(ProductNotFoundException.class);
  }

  @Test
  void addToCart_shouldThrowWhenInsufficientStock() {
    Product product = buildProductWithStock(1);
    when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.of(product));

    assertThatThrownBy(() -> addToCartUseCase.execute(customerId, productId, 5))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Stock insuficiente");
  }

  // ==================== GetCartUseCase ====================

  @Test
  void getCart_shouldReturnEmptyCartWhenNoExistingCart() {
    when(cartRepository.findByCustomerId(customerId)).thenReturn(Optional.empty());

    Cart result = getCartUseCase.execute(customerId);

    assertThat(result).isNotNull();
    assertThat(result.getItems()).isEmpty();
  }

  @Test
  void getCart_shouldReturnExistingCart() {
    Cart existingCart = Cart.create(customerId);
    when(cartRepository.findByCustomerId(customerId)).thenReturn(Optional.of(existingCart));

    Cart result = getCartUseCase.execute(customerId);

    assertThat(result).isNotNull();
    assertThat(result.getCustomerId().getValue()).isEqualTo(customerId);
  }

  // ==================== RemoveFromCartUseCase ====================

  @Test
  void removeFromCart_shouldThrowWhenCartNotFound() {
    when(cartRepository.findByCustomerId(customerId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> removeFromCartUseCase.execute(customerId, UUID.randomUUID()))
        .isInstanceOf(CartNotFoundException.class);
  }

  // ==================== ClearCartUseCase ====================

  @Test
  void clearCart_shouldDeleteCartByCustomerId() {
    clearCartUseCase.execute(customerId);

    verify(cartRepository).deleteByCustomerId(customerId);
  }

  // ==================== UpdateCartItemUseCase ====================

  @Test
  void updateCartItem_shouldThrowWhenCartNotFound() {
    when(cartRepository.findByCustomerId(customerId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> updateCartItemUseCase.execute(customerId, UUID.randomUUID(), 5))
        .isInstanceOf(CartNotFoundException.class);
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

  private Product buildProductWithStock(int stock) {
    return new Product(
        new ProductId(productId),
        new ProductSellerId(UUID.randomUUID()),
        new ProductName("Test Product"),
        new ProductPrice(BigDecimal.valueOf(50.00)),
        new ProductStock(stock),
        java.util.List.of());
  }
}
