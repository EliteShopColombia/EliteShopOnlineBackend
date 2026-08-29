package com.eliteshop.colombia.auth.infrastructure.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;

public class JwtService {

  private final String secret;
  private final long expiration;
  private final String issuer;

  public JwtService(JwtProperties properties) {
    this.secret = properties.getSecret();
    this.expiration = properties.getExpiration();
    this.issuer = properties.getIssuer();
  }

  public String generateToken(String userId, String email, String role) {
    return generateToken(userId, email, role, null);
  }

  public String generateToken(String userId, String email, String role, String sellerId) {
    var builder =
        Jwts.builder()
            .subject(userId)
            .claim("email", email)
            .claim("role", role)
            .issuer(issuer)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expiration))
            .id(UUID.randomUUID().toString())
            .signWith(getSigningKey());

    if (sellerId != null) {
      builder.claim("sellerId", sellerId);
    }

    return builder.compact();
  }

  public Claims validateToken(String token) {
    return Jwts.parser()
        .verifyWith(getSigningKey())
        .requireIssuer(issuer)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public long getExpiration() {
    return System.currentTimeMillis() + expiration;
  }

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
  }
}
