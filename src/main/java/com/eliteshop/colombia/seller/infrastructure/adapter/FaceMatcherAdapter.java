package com.eliteshop.colombia.seller.infrastructure.adapter;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class FaceMatcherAdapter {

  private final WebClient sellerVerificationWebClient;

  @Value("${face-matcher.url:http://face-matcher:5000}")
  private String faceMatcherUrl;

  public FaceMatchResult match(String selfieObject, String documentObject) {
    Map<String, String> request =
        Map.of("selfie_object", selfieObject, "document_object", documentObject);

    return sellerVerificationWebClient
        .post()
        .uri(faceMatcherUrl + "/match")
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
