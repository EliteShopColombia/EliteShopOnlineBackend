package com.eliteshop.colombia.seller.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

@ExtendWith(MockitoExtension.class)
class MinIOBucketResolverTest {

  @Mock private MinIOProperties minIOProperties;
  @Mock private Environment environment;
  @InjectMocks private MinIOBucketResolver resolver;

  @Test
  void shouldReturnTestBucketsWhenLocalProfile() {
    when(environment.getActiveProfiles()).thenReturn(new String[] {"local"});
    MinIOProperties.Bucket bucket = new MinIOProperties.Bucket();
    bucket.setProductImagesTest("test-products");
    bucket.setReviewImagesTest("test-reviews");
    bucket.setVerificationSellersTest("test-verification");
    bucket.setCustomerAvatarsTest("test-avatars");
    bucket.setBusinessAvatarsTest("test-business");
    when(minIOProperties.getBucket()).thenReturn(bucket);

    assertThat(resolver.getProductImagesBucket()).isEqualTo("test-products");
    assertThat(resolver.getReviewImagesBucket()).isEqualTo("test-reviews");
    assertThat(resolver.getVerificationSellersBucket()).isEqualTo("test-verification");
    assertThat(resolver.getCustomerAvatarsBucket()).isEqualTo("test-avatars");
    assertThat(resolver.getBusinessAvatarsBucket()).isEqualTo("test-business");
  }

  @Test
  void shouldReturnTestBucketsWhenDevProfile() {
    when(environment.getActiveProfiles()).thenReturn(new String[] {"dev"});
    MinIOProperties.Bucket bucket = new MinIOProperties.Bucket();
    bucket.setProductImagesTest("test-products-dev");
    when(minIOProperties.getBucket()).thenReturn(bucket);

    assertThat(resolver.getProductImagesBucket()).isEqualTo("test-products-dev");
  }

  @Test
  void shouldReturnProdBucketsWhenProdProfile() {
    when(environment.getActiveProfiles()).thenReturn(new String[] {"prod"});
    MinIOProperties.Bucket bucket = new MinIOProperties.Bucket();
    bucket.setProductImages("prod-products");
    bucket.setProductImagesTest("test-products");
    when(minIOProperties.getBucket()).thenReturn(bucket);

    assertThat(resolver.getProductImagesBucket()).isEqualTo("prod-products");
  }

  @Test
  void shouldReturnProdBucketsWhenNoProfileActive() {
    when(environment.getActiveProfiles()).thenReturn(new String[0]);
    MinIOProperties.Bucket bucket = new MinIOProperties.Bucket();
    bucket.setProductImages("prod-products");
    bucket.setProductImagesTest("test-products");
    when(minIOProperties.getBucket()).thenReturn(bucket);

    assertThat(resolver.getProductImagesBucket()).isEqualTo("prod-products");
  }
}
