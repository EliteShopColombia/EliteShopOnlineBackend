package com.eliteshop.colombia.seller.domain.model;

import com.eliteshop.colombia.seller.domain.exception.SellerInvalidNumberAccountException;
import java.util.Objects;
import java.util.regex.Pattern;
import lombok.Getter;

@Getter
public class SellerNumberAccount {
  private static final Pattern ACCOUNT_PATTERN = Pattern.compile("^[0-9]{1,30}$");
  private static final int MIN_LENGTH = 1;
  private static final int MAX_LENGTH = 30;

  private final String value;

  public SellerNumberAccount(String value) {
    Objects.requireNonNull(value, "El número de cuenta no puede ser nulo");
    String trimmed = value.trim();
    if (trimmed.length() < MIN_LENGTH) {
      throw new SellerInvalidNumberAccountException("El número de cuenta no puede estar vacío");
    }
    if (trimmed.length() > MAX_LENGTH) {
      throw new SellerInvalidNumberAccountException(
          "El número de cuenta no puede exceder " + MAX_LENGTH + " caracteres");
    }
    if (!ACCOUNT_PATTERN.matcher(trimmed).matches()) {
      throw new SellerInvalidNumberAccountException(
          "El número de cuenta solo puede contener dígitos");
    }
    this.value = trimmed;
  }
}
