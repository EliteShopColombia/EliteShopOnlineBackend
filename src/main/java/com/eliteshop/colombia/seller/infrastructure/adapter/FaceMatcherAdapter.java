package com.eliteshop.colombia.seller.infrastructure.adapter;

import com.eliteshop.colombia.seller.infrastructure.config.FaceMatcherProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class FaceMatcherAdapter {

  private final WebClient sellerVerificationWebClient;
  private final FaceMatcherProperties faceMatcherProperties;

  public FaceMatchResult match(String selfieObject, String documentObject) {
    Map<String, String> request =
        Map.of("selfie_object", selfieObject, "document_object", documentObject);

    return sellerVerificationWebClient
        .post()
        .uri(faceMatcherProperties.getUrl() + "/match")
        .bodyValue(request)
        .retrieve()
        .bodyToMono(FaceMatchResult.class)
        .block();
  }

  public record FaceMatchResult(
      boolean match,
      double confidence,
      @JsonProperty("selfie_face_found") boolean selfieFaceFound,
      @JsonProperty("document_face_found") boolean documentFaceFound,
      String message) {}
}
