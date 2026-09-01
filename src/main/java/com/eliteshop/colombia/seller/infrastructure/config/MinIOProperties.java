package com.eliteshop.colombia.seller.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "minio")
public class MinIOProperties {

  @NotBlank(message = "minio.endpoint es requerido")
  private String endpoint;

  @NotBlank(message = "minio.access-key es requerido")
  private String accessKey;

  @NotBlank(message = "minio.secret-key es requerido")
  private String secretKey;

  private Bucket bucket = new Bucket();

  @Data
  public static class Bucket {

    private String verificationSellers;
    private String verificationSellersTest;
    private String productImages;
    private String productImagesTest;
    private String reviewImages;
    private String reviewImagesTest;
    private String customerAvatars;
    private String customerAvatarsTest;
    private String businessAvatars;
    private String businessAvatarsTest;
  }
}
