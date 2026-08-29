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
}
