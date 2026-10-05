package com.eliteshop.colombia.seller.domain.model.verification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** ID de tarea devuelto por el microservicio de verificación al encolar. */
@RequiredArgsConstructor
@Getter
public class SellerVerificationTaskId {
  private final String value;
}
