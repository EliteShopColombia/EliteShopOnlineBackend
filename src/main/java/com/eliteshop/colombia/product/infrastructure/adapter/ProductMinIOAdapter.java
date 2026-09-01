package com.eliteshop.colombia.product.infrastructure.adapter;

import com.eliteshop.colombia.seller.infrastructure.config.MinIOBucketResolver;
import com.eliteshop.colombia.shared.exception.StorageException;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import java.io.InputStream;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductMinIOAdapter {

  private final MinioClient minioClient;
  private final MinIOBucketResolver bucketResolver;

  public String uploadImage(String productId, String filename, InputStream stream) {
    String bucket = bucketResolver.getProductImagesBucket();
    String objectKey = "products/" + productId + "/" + UUID.randomUUID() + "_" + filename;
    try {
      minioClient.putObject(
          PutObjectArgs.builder().bucket(bucket).object(objectKey).stream(stream, -1, 10485760)
              .contentType("image/jpeg")
              .build());
    } catch (Exception e) {
      throw new StorageException("Error subiendo imagen de producto a MinIO", e);
    }
    return objectKey;
  }

  public InputStream downloadImage(String objectKey) {
    String bucket = bucketResolver.getProductImagesBucket();
    try {
      return minioClient.getObject(
          GetObjectArgs.builder().bucket(bucket).object(objectKey).build());
    } catch (Exception e) {
      throw new StorageException("Error descargando imagen de producto de MinIO", e);
    }
  }
}
