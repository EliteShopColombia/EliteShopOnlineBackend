package com.eliteshop.colombia.review.infrastructure.adapter;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import java.io.InputStream;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewMinIOAdapter {

  private final MinioClient minioClient;

  @Value("${minio.bucket.review-images:eliteshop-product-review-images}")
  private String bucket;

  public String uploadImage(String reviewId, String filename, InputStream stream) {
    String objectKey = "reviews/" + reviewId + "/" + UUID.randomUUID() + "_" + filename;
    try {
      minioClient.putObject(
          PutObjectArgs.builder().bucket(bucket).object(objectKey).stream(stream, -1, 10485760)
              .contentType("image/jpeg")
              .build());
    } catch (Exception e) {
      throw new RuntimeException("Error subiendo imagen de review a MinIO", e);
    }
    return objectKey;
  }

  public InputStream downloadImage(String objectKey) {
    try {
      return minioClient.getObject(
          GetObjectArgs.builder().bucket(bucket).object(objectKey).build());
    } catch (Exception e) {
      throw new RuntimeException("Error descargando imagen de review de MinIO", e);
    }
  }
}
