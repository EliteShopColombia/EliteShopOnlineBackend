package com.eliteshop.colombia.seller.infrastructure.adapter;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MinIOAdapter {

  private final MinioClient minioClient;

  @Value("${minio.bucket.verification-sellers:eliteshop-sellers}")
  private String bucket;

  public String uploadDocument(String sellerId, String filename, InputStream stream) {
    String objectKey = "sellers/" + sellerId + "/document.jpg";
    try {
      minioClient.putObject(
          PutObjectArgs.builder().bucket(bucket).object(objectKey).stream(stream, -1, 10485760)
              .contentType("image/jpeg")
              .build());
    } catch (Exception e) {
      throw new RuntimeException("Error subiendo documento a MinIO", e);
    }
    return objectKey;
  }

  public String uploadSelfie(String sellerId, String filename, InputStream stream) {
    String objectKey = "sellers/" + sellerId + "/selfie.jpg";
    try {
      minioClient.putObject(
          PutObjectArgs.builder().bucket(bucket).object(objectKey).stream(stream, -1, 10485760)
              .contentType("image/jpeg")
              .build());
    } catch (Exception e) {
      throw new RuntimeException("Error subiendo selfie a MinIO", e);
    }
    return objectKey;
  }

  public InputStream downloadImage(String objectKey) {
    try {
      return minioClient.getObject(
          GetObjectArgs.builder().bucket(bucket).object(objectKey).build());
    } catch (Exception e) {
      throw new RuntimeException("Error descargando imagen de MinIO", e);
    }
  }
}
