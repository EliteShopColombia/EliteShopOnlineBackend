package com.eliteshop.colombia.payment.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "payments.epayco")
public class EpaycoProperties {

  @NotBlank(message = "La publicKey de ePayco es requerida")
  private String publicKey;

  @NotBlank(message = "La privateKey de ePayco es requerida")
  private String privateKey;

  private String environment;

  @NotBlank(message = "La apifyBaseUrl de ePayco es requerida")
  private String apifyBaseUrl;

  private String apiBaseUrl = "https://api.secure.payco.co";

  private String responseUrl = "http://localhost:5173/order";

  public boolean isSandbox() {
    return "sandbox".equals(environment);
  }
}
