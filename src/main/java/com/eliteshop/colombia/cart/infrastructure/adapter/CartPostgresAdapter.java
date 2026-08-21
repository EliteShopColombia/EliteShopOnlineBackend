package com.eliteshop.colombia.cart.infrastructure.adapter;

import com.eliteshop.colombia.cart.domain.model.*;
import com.eliteshop.colombia.cart.domain.repository.CartRepository;
import com.eliteshop.colombia.cart.infrastructure.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CartPostgresAdapter implements CartRepository {

  private final CartJpaRepository cartJpaRepository;
  private final CartItemJpaRepository cartItemJpaRepository;

  @Override
  @Transactional
  public Cart save(Cart cart) {
    CartEntity cartEntity = toCartEntity(cart);
    cartJpaRepository.save(cartEntity);

    // Eliminar items existentes y guardar los nuevos
    cartItemJpaRepository.deleteByCartId(cart.getId().getValue());

    if (cart.getItems() != null && !cart.getItems().isEmpty()) {
      List<CartItemEntity> itemEntities =
          cart.getItems().stream().map(this::toCartItemEntity).collect(Collectors.toList());
      cartItemJpaRepository.saveAll(itemEntities);
    }

    return cart;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Cart> findById(CartId id) {
    return cartJpaRepository.findById(id.getValue()).map(this::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Cart> findByCustomerId(UUID customerId) {
    return cartJpaRepository.findByCustomerId(customerId).map(this::toDomain);
  }

  @Override
  @Transactional
  public void deleteById(CartId id) {
    cartItemJpaRepository.deleteByCartId(id.getValue());
    cartJpaRepository.deleteById(id.getValue());
  }

  @Override
  @Transactional
  public void deleteByCustomerId(UUID customerId) {
    cartJpaRepository
        .findByCustomerId(customerId)
        .ifPresent(
            cart -> {
              cartItemJpaRepository.deleteByCartId(cart.getId());
              cartJpaRepository.deleteByCustomerId(customerId);
            });
  }

  private Cart toDomain(CartEntity entity) {
    if (entity == null) return null;

    List<CartItemEntity> itemEntities = cartItemJpaRepository.findByCartId(entity.getId());
    List<CartItem> items =
        itemEntities.stream().map(this::toCartItemDomain).collect(Collectors.toList());

    return new Cart(
        new CartId(entity.getId()),
        new CartCustomerId(entity.getCustomerId()),
        new ArrayList<>(items),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }

  private CartItem toCartItemDomain(CartItemEntity entity) {
    if (entity == null) return null;
    return new CartItem(
        new CartItemId(entity.getId()),
        new CartId(entity.getCartId()),
        new CartItemProductId(entity.getProductId()),
        new CartItemQuantity(entity.getQuantity()),
        new CartItemUnitPrice(entity.getUnitPrice()),
        entity.getAddedAt());
  }

  private CartEntity toCartEntity(Cart domain) {
    if (domain == null) return null;
    CartEntity entity = new CartEntity();
    entity.setId(domain.getId().getValue());
    entity.setCustomerId(domain.getCustomerId().getValue());
    entity.setCreatedAt(domain.getCreatedAt());
    entity.setUpdatedAt(LocalDateTime.now());
    return entity;
  }

  private CartItemEntity toCartItemEntity(CartItem domain) {
    if (domain == null) return null;
    CartItemEntity entity = new CartItemEntity();
    entity.setId(domain.getId().getValue());
    entity.setCartId(domain.getCartId().getValue());
    entity.setProductId(domain.getProductId().getValue());
    entity.setQuantity(domain.getQuantity().getValue());
    entity.setUnitPrice(domain.getUnitPrice().getValue());
    entity.setAddedAt(domain.getAddedAt());
    return entity;
  }
}
