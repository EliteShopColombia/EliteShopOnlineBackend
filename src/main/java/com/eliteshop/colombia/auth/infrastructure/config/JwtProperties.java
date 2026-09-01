package com.eliteshop.colombia.auth.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

  @NotBlank(message = "jwt.secret es requerido")
  private String secret;

  private long expiration = 86400000;

  private String issuer = "eliteshop-backend";
}
