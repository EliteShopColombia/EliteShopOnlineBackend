package com.eliteshop.colombia.customer.infrastructure.adapter;

import com.eliteshop.colombia.seller.infrastructure.config.MinIOBucketResolver;
import com.eliteshop.colombia.shared.exception.StorageException;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerMinIOAdapter {

  private final MinioClient minioClient;
  private final MinIOBucketResolver bucketResolver;

  public String uploadAvatar(String customerId, String filename, InputStream stream) {
    String bucket = bucketResolver.getCustomerAvatarsBucket();
    String objectKey = "avatars/customers/" + customerId + "/" + filename;
    try {
      minioClient.putObject(
          PutObjectArgs.builder().bucket(bucket).object(objectKey).stream(stream, -1, 10485760)
              .contentType("image/jpeg")
              .build());
    } catch (Exception e) {
      throw new StorageException("Error subiendo avatar de cliente a MinIO", e);
    }
    return objectKey;
  }

  public InputStream downloadAvatar(String objectKey) {
    String bucket = bucketResolver.getCustomerAvatarsBucket();
    try {
      return minioClient.getObject(
          GetObjectArgs.builder().bucket(bucket).object(objectKey).build());
    } catch (Exception e) {
      throw new StorageException("Error descargando avatar de cliente de MinIO", e);
    }
  }

  public void deleteAvatar(String objectKey) {
    String bucket = bucketResolver.getCustomerAvatarsBucket();
    try {
      minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectKey).build());
    } catch (Exception e) {
      throw new StorageException("Error eliminando avatar de cliente de MinIO", e);
    }
  }
}
