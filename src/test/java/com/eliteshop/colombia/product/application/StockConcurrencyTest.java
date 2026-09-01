package com.eliteshop.colombia.product.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.eliteshop.colombia.product.domain.model.ProductId;
import com.eliteshop.colombia.product.domain.repository.ProductImageRepository;
import com.eliteshop.colombia.product.infrastructure.adapter.ProductPostgresAdapter;
import com.eliteshop.colombia.product.infrastructure.mapper.ProductMapper;
import com.eliteshop.colombia.product.infrastructure.persistence.ProductEntity;
import com.eliteshop.colombia.product.infrastructure.persistence.ProductJpaRepository;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockConcurrencyTest {

  @Mock private ProductJpaRepository jpaRepository;
  @Mock private ProductMapper mapper;
  @Mock private ProductImageRepository productImageRepository;
  private ProductPostgresAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new ProductPostgresAdapter(jpaRepository, mapper, productImageRepository);
  }

  @Test
  void shouldSucceedWhenStockIsAvailable() {
    UUID productId = UUID.randomUUID();
    when(jpaRepository.reduceStock(productId, 2)).thenReturn(1);

    adapter.reduceStock(new ProductId(productId), 2);
  }

  @Test
  void shouldThrowWhenStockIsInsufficient() {
    UUID productId = UUID.randomUUID();
    when(jpaRepository.reduceStock(productId, 10)).thenReturn(0);
    when(jpaRepository.findById(productId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> adapter.reduceStock(new ProductId(productId), 10))
        .isInstanceOf(
            com.eliteshop.colombia.product.domain.exception.StockInsufficientException.class)
        .hasMessageContaining("Stock insuficiente");
  }

  @Test
  void shouldThrowWithProductNameWhenInsufficient() {
    UUID productId = UUID.randomUUID();
    ProductEntity entity = new ProductEntity();
    entity.setName("Laptop Gamer");

    when(jpaRepository.reduceStock(productId, 5)).thenReturn(0);
    when(jpaRepository.findById(productId)).thenReturn(Optional.of(entity));

    assertThatThrownBy(() -> adapter.reduceStock(new ProductId(productId), 5))
        .isInstanceOf(
            com.eliteshop.colombia.product.domain.exception.StockInsufficientException.class)
        .hasMessageContaining("Laptop Gamer");
  }

  @Test
  void shouldDelegateToRepositoryWhenRestoringStock() {
    UUID productId = UUID.randomUUID();

    adapter.restoreStock(new ProductId(productId), 3);
  }

  @Test
  void shouldOnlySucceedForAvailableQuantityWhenConcurrent() throws Exception {
    UUID productId = UUID.randomUUID();
    AtomicInteger successCount = new AtomicInteger(0);

    when(jpaRepository.reduceStock(eq(productId), eq(1)))
        .thenAnswer(
            inv -> {
              if (successCount.get() < 5) {
                successCount.incrementAndGet();
                return 1;
              }
              return 0;
            });

    int threadCount = 10;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(1);
    AtomicInteger successfulReductions = new AtomicInteger(0);
    AtomicInteger failedReductions = new AtomicInteger(0);

    for (int i = 0; i < threadCount; i++) {
      executor.submit(
          () -> {
            try {
              latch.await();
              adapter.reduceStock(new ProductId(productId), 1);
              successfulReductions.incrementAndGet();
            } catch (com.eliteshop.colombia.product.domain.exception.StockInsufficientException e) {
              failedReductions.incrementAndGet();
            } catch (Exception e) {
              // ignore
            }
          });
    }

    latch.countDown();
    executor.shutdown();
    executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);

    assertThat(successfulReductions.get() + failedReductions.get()).isEqualTo(threadCount);
  }
}
