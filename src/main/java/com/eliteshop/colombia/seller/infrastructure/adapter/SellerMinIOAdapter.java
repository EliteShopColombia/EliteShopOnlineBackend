package com.eliteshop.colombia.seller.infrastructure.adapter;

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
public class SellerMinIOAdapter {

  private final MinioClient minioClient;
  private final MinIOBucketResolver bucketResolver;

  public String uploadAvatar(String sellerId, String filename, InputStream stream) {
    String bucket = bucketResolver.getBusinessAvatarsBucket();
    String objectKey = "avatars/sellers/" + sellerId + "/" + filename;
    try {
      minioClient.putObject(
          PutObjectArgs.builder().bucket(bucket).object(objectKey).stream(stream, -1, 10485760)
              .contentType("image/jpeg")
              .build());
    } catch (Exception e) {
      throw new StorageException("Error subiendo avatar de vendedor a MinIO", e);
    }
    return objectKey;
  }

  public InputStream downloadAvatar(String objectKey) {
    String bucket = bucketResolver.getBusinessAvatarsBucket();
    try {
      return minioClient.getObject(
          GetObjectArgs.builder().bucket(bucket).object(objectKey).build());
    } catch (Exception e) {
      throw new StorageException("Error descargando avatar de vendedor de MinIO", e);
    }
  }

  public void deleteAvatar(String objectKey) {
    String bucket = bucketResolver.getBusinessAvatarsBucket();
    try {
      minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectKey).build());
    } catch (Exception e) {
      throw new StorageException("Error eliminando avatar de vendedor de MinIO", e);
    }
  }
}
