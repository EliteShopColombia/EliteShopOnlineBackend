package com.eliteshop.colombia.seller.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "face-matcher")
public class FaceMatcherProperties {

  @NotBlank(message = "face-matcher.url es requerido")
  private String url;

  /**
   * Secreto compartido que se envía en la cabecera {@code X-Gateway-Secret}. El microservicio
   * face-matcher lo exige para todos los endpoints salvo /health.
   */
  private String secret;
}
