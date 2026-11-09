package com.eliteshop.colombia.seller.domain.model.verification;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Claves de MinIO de los frames de la ráfaga de la selfie, separadas por coma. Se usan para el
 * análisis temporal de liveness (parpadeo y movimiento de cabeza).
 */
@RequiredArgsConstructor
@Getter
public class SellerVerificationFramesMinioKeys {
  private final String value;

  public List<String> asList() {
    if (value == null || value.isBlank()) {
      return List.of();
    }
    return Arrays.stream(value.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
  }

  public static SellerVerificationFramesMinioKeys fromList(List<String> keys) {
    return new SellerVerificationFramesMinioKeys(String.join(",", keys));
  }
}
