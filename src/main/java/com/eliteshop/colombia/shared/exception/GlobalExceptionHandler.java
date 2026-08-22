package com.eliteshop.colombia.shared.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidationErrors(
      MethodArgumentNotValidException ex) {
    Map<String, String> fieldErrors = new HashMap<>();
    for (FieldError error : ex.getBindingResult().getFieldErrors()) {
      fieldErrors.put(error.getField(), error.getDefaultMessage());
    }

    Map<String, Object> response = new HashMap<>();
    response.put("timestamp", LocalDateTime.now().toString());
    response.put("status", HttpStatus.BAD_REQUEST.value());
    response.put("error", "Validación fallida");
    response.put("fieldErrors", fieldErrors);

    return ResponseEntity.badRequest().body(response);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
    Map<String, Object> response = new HashMap<>();
    response.put("timestamp", LocalDateTime.now().toString());
    response.put("status", HttpStatus.BAD_REQUEST.value());
    response.put("error", ex.getMessage());

    return ResponseEntity.badRequest().body(response);
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<Map<String, Object>> handleDomainExceptions(RuntimeException ex) {
    Map<String, Object> response = new HashMap<>();
    response.put("timestamp", LocalDateTime.now().toString());
    response.put("error", ex.getMessage());

    if (ex instanceof com.eliteshop.colombia.seller.domain.exception.SellerNotFoundException) {
      response.put("status", HttpStatus.NOT_FOUND.value());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    if (ex instanceof com.eliteshop.colombia.seller.domain.exception.SellerAlreadyExistsException) {
      response.put("status", HttpStatus.CONFLICT.value());
      return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    if (ex instanceof com.eliteshop.colombia.product.domain.exception.ProductNotFoundException) {
      response.put("status", HttpStatus.NOT_FOUND.value());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    if (ex
        instanceof com.eliteshop.colombia.product.domain.exception.ProductAlreadyExistsException) {
      response.put("status", HttpStatus.CONFLICT.value());
      return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    if (ex instanceof com.eliteshop.colombia.review.domain.exception.ReviewNotFoundException) {
      response.put("status", HttpStatus.NOT_FOUND.value());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    if (ex instanceof com.eliteshop.colombia.cart.domain.exception.CartNotFoundException
        || ex instanceof com.eliteshop.colombia.cart.domain.exception.CartItemNotFoundException
        || ex instanceof com.eliteshop.colombia.payment.domain.exception.PaymentNotFoundException) {
      response.put("status", HttpStatus.NOT_FOUND.value());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    if (ex instanceof com.eliteshop.colombia.checkout.domain.exception.InsufficientStockException) {
      response.put("status", HttpStatus.CONFLICT.value());
      return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    if (ex
        instanceof com.eliteshop.colombia.checkout.domain.exception.CannotBuyOwnProductException) {
      response.put("status", HttpStatus.FORBIDDEN.value());
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    if (ex instanceof com.eliteshop.colombia.order.domain.exception.OrderAccessDeniedException) {
      response.put("status", HttpStatus.FORBIDDEN.value());
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    if (ex instanceof com.eliteshop.colombia.checkout.domain.exception.PaymentFailedException) {
      response.put("status", HttpStatus.PAYMENT_REQUIRED.value());
      return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(response);
    }

    response.put("status", HttpStatus.BAD_REQUEST.value());
    return ResponseEntity.badRequest().body(response);
  }
}
