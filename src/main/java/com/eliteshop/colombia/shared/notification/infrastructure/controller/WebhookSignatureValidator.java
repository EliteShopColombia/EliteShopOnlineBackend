package com.eliteshop.colombia.shared.notification.infrastructure.controller;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class WebhookSignatureValidator {

  private static final String HMAC_SHA256 = "HmacSHA256";
  private static final String SHA256_PREFIX = "sha256=";

  private WebhookSignatureValidator() {}

  public static boolean isValid(String payload, String signatureHeader, String secret) {
    if (signatureHeader == null || !signatureHeader.startsWith(SHA256_PREFIX)) {
      return false;
    }

    try {
      Mac mac = Mac.getInstance(HMAC_SHA256);
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
      byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
      String expectedSignature = SHA256_PREFIX + bytesToHex(hash);

      return MessageDigest.isEqual(
          expectedSignature.getBytes(StandardCharsets.UTF_8),
          signatureHeader.getBytes(StandardCharsets.UTF_8));
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      return false;
    }
  }

  private static String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }
}
