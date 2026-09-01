package com.eliteshop.colombia.seller.infrastructure.adapter;

import com.eliteshop.colombia.seller.infrastructure.config.MinIOBucketResolver;
import com.eliteshop.colombia.shared.exception.StorageException;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MinIOAdapter {

  private final MinioClient minioClient;
  private final MinIOBucketResolver bucketResolver;

  public String uploadDocument(String sellerId, String filename, InputStream stream) {
    String bucket = bucketResolver.getVerificationSellersBucket();
    String objectKey = "sellers/" + sellerId + "/document.jpg";
    try {
      minioClient.putObject(
          PutObjectArgs.builder().bucket(bucket).object(objectKey).stream(stream, -1, 10485760)
              .contentType("image/jpeg")
              .build());
    } catch (Exception e) {
      throw new StorageException("Error subiendo documento a MinIO", e);
    }
    return objectKey;
  }

  public String uploadSelfie(String sellerId, String filename, InputStream stream) {
    String bucket = bucketResolver.getVerificationSellersBucket();
    String objectKey = "sellers/" + sellerId + "/selfie.jpg";
    try {
      minioClient.putObject(
          PutObjectArgs.builder().bucket(bucket).object(objectKey).stream(stream, -1, 10485760)
              .contentType("image/jpeg")
              .build());
    } catch (Exception e) {
      throw new StorageException("Error subiendo selfie a MinIO", e);
    }
    return objectKey;
  }

  public InputStream downloadImage(String objectKey) {
    String bucket = bucketResolver.getVerificationSellersBucket();
    try {
      return minioClient.getObject(
          GetObjectArgs.builder().bucket(bucket).object(objectKey).build());
    } catch (Exception e) {
      throw new StorageException("Error descargando imagen de MinIO", e);
    }
  }
}
