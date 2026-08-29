package com.eliteshop.colombia.seller.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MinIOBucketResolver {

  private final MinIOProperties minIOProperties;
  private final Environment environment;

  public String getProductImagesBucket() {
    return isTestProfile()
        ? minIOProperties.getBucket().getProductImagesTest()
        : minIOProperties.getBucket().getProductImages();
  }

  public String getReviewImagesBucket() {
    return isTestProfile()
        ? minIOProperties.getBucket().getReviewImagesTest()
        : minIOProperties.getBucket().getReviewImages();
  }

  public String getVerificationSellersBucket() {
    return isTestProfile()
        ? minIOProperties.getBucket().getVerificationSellersTest()
        : minIOProperties.getBucket().getVerificationSellers();
  }

  public String getCustomerAvatarsBucket() {
    return isTestProfile()
        ? minIOProperties.getBucket().getCustomerAvatarsTest()
        : minIOProperties.getBucket().getCustomerAvatars();
  }

  public String getBusinessAvatarsBucket() {
    return isTestProfile()
        ? minIOProperties.getBucket().getBusinessAvatarsTest()
        : minIOProperties.getBucket().getBusinessAvatars();
  }

  private boolean isTestProfile() {
    String[] activeProfiles = environment.getActiveProfiles();
    for (String profile : activeProfiles) {
      if ("dev".equals(profile) || "local".equals(profile)) {
        return true;
      }
    }
    return false;
  }
}
