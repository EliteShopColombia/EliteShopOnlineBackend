package com.eliteshop.colombia.seller.domain.model;

import com.eliteshop.colombia.seller.domain.exception.SellerInvalidTradeDepartmentException;
import java.util.Objects;
import lombok.Getter;

@Getter
public class SellerTradeDepartment {
  private static final int MAX_LENGTH = 60;

  private final String value;

  public SellerTradeDepartment(String value) {
    Objects.requireNonNull(value, "El departamento no puede ser nulo");
    String trimmed = value.trim();
    if (trimmed.isEmpty()) {
      throw new SellerInvalidTradeDepartmentException("El departamento no puede estar vacío");
    }
    if (trimmed.length() > MAX_LENGTH) {
      throw new SellerInvalidTradeDepartmentException(
          "El departamento no puede exceder "
              + MAX_LENGTH
              + " caracteres (ej: Antioquia, Valle del Cauca)");
    }
    this.value = trimmed;
  }
}
