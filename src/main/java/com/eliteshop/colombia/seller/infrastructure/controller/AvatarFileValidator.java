package com.eliteshop.colombia.seller.infrastructure.controller;

import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class AvatarFileValidator {

  private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png");
  private static final long MAX_SIZE = 5 * 1024 * 1024;

  public void validate(MultipartFile file) {
    if (file.isEmpty()) {
      throw new IllegalArgumentException("El archivo esta vacio");
    }
    if (file.getSize() > MAX_SIZE) {
      throw new IllegalArgumentException("La imagen excede 5MB");
    }
    String contentType = file.getContentType();
    if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
      throw new IllegalArgumentException("Formato no soportado. Solo se permiten JPG y PNG");
    }
  }
}
