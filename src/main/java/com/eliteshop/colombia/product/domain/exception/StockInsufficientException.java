package com.eliteshop.colombia.product.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class StockInsufficientException extends RuntimeException {
  public static final String CODE = "STOCK_INSUFFICIENT";

  public StockInsufficientException(String message) {
    super(message);
  }
}
