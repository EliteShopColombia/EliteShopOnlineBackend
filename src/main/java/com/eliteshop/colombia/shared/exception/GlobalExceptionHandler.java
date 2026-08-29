package com.eliteshop.colombia.shared.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
    Map<String, String> fieldErrors = new HashMap<>();
    for (FieldError error : ex.getBindingResult().getFieldErrors()) {
      fieldErrors.put(error.getField(), error.getDefaultMessage());
    }
    ErrorResponse response =
        ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validacion fallida")
            .code("VALIDATION_FAILED")
            .fieldErrors(fieldErrors)
            .build();
    return ResponseEntity.badRequest().body(response);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
    Map<String, String> fieldErrors =
        ex.getConstraintViolations().stream()
            .collect(
                Collectors.toMap(
                    v -> v.getPropertyPath().toString(),
                    ConstraintViolation::getMessage,
                    (a, b) -> a));
    ErrorResponse response =
        ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validacion fallida")
            .code("VALIDATION_FAILED")
            .fieldErrors(fieldErrors)
            .build();
    return ResponseEntity.badRequest().body(response);
  }

  @ExceptionHandler(com.eliteshop.colombia.auth.domain.exception.InvalidTokenException.class)
  public ResponseEntity<ErrorResponse> handleInvalidToken(
      com.eliteshop.colombia.auth.domain.exception.InvalidTokenException ex) {
    log.warn("Token invalido: {}", ex.getMessage());
    return unauthorized("Token invalido o expirado", "INVALID_TOKEN");
  }

  @ExceptionHandler(com.eliteshop.colombia.auth.domain.exception.InvalidCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleInvalidCredentials(
      com.eliteshop.colombia.auth.domain.exception.InvalidCredentialsException ex) {
    log.warn("Credenciales invalidas: {}", ex.getMessage());
    return unauthorized("Credenciales invalidas", "INVALID_CREDENTIALS");
  }

  @ExceptionHandler(com.eliteshop.colombia.seller.domain.exception.SellerNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleSellerNotFound(
      com.eliteshop.colombia.seller.domain.exception.SellerNotFoundException ex) {
    log.warn("Vendedor no encontrado: {}", ex.getMessage());
    return notFound("Vendedor no encontrado", "SELLER_NOT_FOUND");
  }

  @ExceptionHandler(
      com.eliteshop.colombia.seller.domain.exception.SellerAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleSellerAlreadyExists(
      com.eliteshop.colombia.seller.domain.exception.SellerAlreadyExistsException ex) {
    log.warn("Vendedor ya existe: {}", ex.getMessage());
    return conflict("El vendedor ya existe", "SELLER_ALREADY_EXISTS");
  }

  @ExceptionHandler(com.eliteshop.colombia.product.domain.exception.ProductNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleProductNotFound(
      com.eliteshop.colombia.product.domain.exception.ProductNotFoundException ex) {
    log.warn("Producto no encontrado: {}", ex.getMessage());
    return notFound("Producto no encontrado", "PRODUCT_NOT_FOUND");
  }

  @ExceptionHandler(
      com.eliteshop.colombia.product.domain.exception.ProductAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleProductAlreadyExists(
      com.eliteshop.colombia.product.domain.exception.ProductAlreadyExistsException ex) {
    log.warn("Producto ya existe: {}", ex.getMessage());
    return conflict("El producto ya existe", "PRODUCT_ALREADY_EXISTS");
  }

  @ExceptionHandler(com.eliteshop.colombia.review.domain.exception.ReviewNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleReviewNotFound(
      com.eliteshop.colombia.review.domain.exception.ReviewNotFoundException ex) {
    log.warn("Review no encontrada: {}", ex.getMessage());
    return notFound("Review no encontrada", "REVIEW_NOT_FOUND");
  }

  @ExceptionHandler(com.eliteshop.colombia.cart.domain.exception.CartNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleCartNotFound(
      com.eliteshop.colombia.cart.domain.exception.CartNotFoundException ex) {
    log.warn("Carrito no encontrado: {}", ex.getMessage());
    return notFound("Carrito no encontrado", "CART_NOT_FOUND");
  }

  @ExceptionHandler(com.eliteshop.colombia.cart.domain.exception.CartItemNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleCartItemNotFound(
      com.eliteshop.colombia.cart.domain.exception.CartItemNotFoundException ex) {
    log.warn("Item del carrito no encontrado: {}", ex.getMessage());
    return notFound("Item del carrito no encontrado", "CART_ITEM_NOT_FOUND");
  }

  @ExceptionHandler(com.eliteshop.colombia.payment.domain.exception.PaymentNotFoundException.class)
  public ResponseEntity<ErrorResponse> handlePaymentNotFound(
      com.eliteshop.colombia.payment.domain.exception.PaymentNotFoundException ex) {
    log.warn("Pago no encontrado: {}", ex.getMessage());
    return notFound("Pago no encontrado", "PAYMENT_NOT_FOUND");
  }

  @ExceptionHandler(com.eliteshop.colombia.order.domain.exception.OrderNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleOrderNotFound(
      com.eliteshop.colombia.order.domain.exception.OrderNotFoundException ex) {
    log.warn("Orden no encontrada: {}", ex.getMessage());
    return notFound("Orden no encontrada", "ORDER_NOT_FOUND");
  }

  @ExceptionHandler(
      com.eliteshop.colombia.customer.domain.exception.CustomerNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleCustomerNotFound(
      com.eliteshop.colombia.customer.domain.exception.CustomerNotFoundException ex) {
    log.warn("Cliente no encontrado: {}", ex.getMessage());
    return notFound("Cliente no encontrado", "CUSTOMER_NOT_FOUND");
  }

  @ExceptionHandler(com.eliteshop.colombia.customer.domain.exception.CustomerExistException.class)
  public ResponseEntity<ErrorResponse> handleCustomerAlreadyExists(
      com.eliteshop.colombia.customer.domain.exception.CustomerExistException ex) {
    log.warn("Cliente ya existe: {}", ex.getMessage());
    return conflict("El cliente ya existe", "CUSTOMER_ALREADY_EXISTS");
  }

  @ExceptionHandler(com.eliteshop.colombia.order.domain.exception.OrderAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleOrderAlreadyExists(
      com.eliteshop.colombia.order.domain.exception.OrderAlreadyExistsException ex) {
    log.warn("Orden ya existe: {}", ex.getMessage());
    return conflict("La orden ya existe", "ORDER_ALREADY_EXISTS");
  }

  @ExceptionHandler(
      com.eliteshop.colombia.checkout.domain.exception.InsufficientStockException.class)
  public ResponseEntity<ErrorResponse> handleInsufficientStock(
      com.eliteshop.colombia.checkout.domain.exception.InsufficientStockException ex) {
    log.warn("Stock insuficiente: {}", ex.getMessage());
    return conflict("Stock insuficiente", "INSUFFICIENT_STOCK");
  }

  @ExceptionHandler(com.eliteshop.colombia.checkout.domain.exception.EmptyCartException.class)
  public ResponseEntity<ErrorResponse> handleEmptyCart(
      com.eliteshop.colombia.checkout.domain.exception.EmptyCartException ex) {
    log.warn("Carrito vacio: {}", ex.getMessage());
    return badRequest("El carrito esta vacio", "EMPTY_CART");
  }

  @ExceptionHandler(com.eliteshop.colombia.checkout.domain.exception.CvvRequiredException.class)
  public ResponseEntity<ErrorResponse> handleCvvRequired(
      com.eliteshop.colombia.checkout.domain.exception.CvvRequiredException ex) {
    log.warn("CVV requerido: {}", ex.getMessage());
    return badRequest("El CVV es requerido", "CVV_REQUIRED");
  }

  @ExceptionHandler(
      com.eliteshop.colombia.checkout.domain.exception.CannotBuyOwnStoreException.class)
  public ResponseEntity<ErrorResponse> handleCannotBuyOwnStore(
      com.eliteshop.colombia.checkout.domain.exception.CannotBuyOwnStoreException ex) {
    log.warn("Intento de compra propia: {}", ex.getMessage());
    return forbidden("No puedes comprar productos de tu propia tienda", "CANNOT_BUY_OWN_STORE");
  }

  @ExceptionHandler(
      com.eliteshop.colombia.checkout.domain.exception.CannotBuyOwnProductException.class)
  public ResponseEntity<ErrorResponse> handleCannotBuyOwnProduct(
      com.eliteshop.colombia.checkout.domain.exception.CannotBuyOwnProductException ex) {
    log.warn("Intento de compra propia: {}", ex.getMessage());
    return forbidden("No puedes comprar tu propio producto", "CANNOT_BUY_OWN_PRODUCT");
  }

  @ExceptionHandler(com.eliteshop.colombia.checkout.domain.exception.PaymentFailedException.class)
  public ResponseEntity<ErrorResponse> handlePaymentFailed(
      com.eliteshop.colombia.checkout.domain.exception.PaymentFailedException ex) {
    log.warn("Pago fallido: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
        .body(
            ErrorResponse.of(
                HttpStatus.PAYMENT_REQUIRED.value(),
                "El pago no pudo ser procesado",
                "PAYMENT_FAILED"));
  }

  @ExceptionHandler(com.eliteshop.colombia.order.domain.exception.OrderAccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleOrderAccessDenied(
      com.eliteshop.colombia.order.domain.exception.OrderAccessDeniedException ex) {
    log.warn("Acceso denegado a orden: {}", ex.getMessage());
    return forbidden("No tienes acceso a esta orden", "ORDER_ACCESS_DENIED");
  }

  @ExceptionHandler(ResourceAccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleResourceAccessDenied(
      ResourceAccessDeniedException ex) {
    log.warn("Acceso denegado: {}", ex.getMessage());
    return forbidden("No tienes acceso a este recurso", "RESOURCE_ACCESS_DENIED");
  }

  @ExceptionHandler(
      com.eliteshop.colombia.order.domain.exception.InvalidOrderStatusTransitionException.class)
  public ResponseEntity<ErrorResponse> handleInvalidOrderTransition(
      com.eliteshop.colombia.order.domain.exception.InvalidOrderStatusTransitionException ex) {
    log.warn("Transicion de estado invalida: {}", ex.getMessage());
    return badRequest("Transicion de estado no permitida", "INVALID_ORDER_STATUS_TRANSITION");
  }

  @ExceptionHandler(StorageException.class)
  public ResponseEntity<ErrorResponse> handleStorageException(StorageException ex) {
    log.error("Error de almacenamiento: {}", ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
        .body(
            ErrorResponse.of(
                HttpStatus.BAD_GATEWAY.value(),
                "Error en el servicio de almacenamiento",
                "STORAGE_ERROR"));
  }

  @ExceptionHandler(
      com.eliteshop.colombia.payment.domain.exception.PaymentAlreadyProcessedException.class)
  public ResponseEntity<ErrorResponse> handlePaymentAlreadyProcessed(
      com.eliteshop.colombia.payment.domain.exception.PaymentAlreadyProcessedException ex) {
    log.warn("Pago ya procesado: {}", ex.getMessage());
    return conflict("El pago ya fue procesado", "PAYMENT_ALREADY_PROCESSED");
  }

  @ExceptionHandler(com.eliteshop.colombia.payment.domain.exception.PaymentGatewayException.class)
  public ResponseEntity<ErrorResponse> handlePaymentGateway(
      com.eliteshop.colombia.payment.domain.exception.PaymentGatewayException ex) {
    log.error("Error en pasarela de pago: {}", ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
        .body(
            ErrorResponse.of(
                HttpStatus.BAD_GATEWAY.value(),
                "Error en la pasarela de pago",
                "PAYMENT_GATEWAY_ERROR"));
  }

  @ExceptionHandler(
      com.eliteshop.colombia.shared.notification.domain.exception.NotificationFailedException.class)
  public ResponseEntity<ErrorResponse> handleNotificationFailed(
      com.eliteshop.colombia.shared.notification.domain.exception.NotificationFailedException ex) {
    log.error("Error en notificación: {}", ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(
            ErrorResponse.of(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Error en el servicio de notificaciónes",
                "NOTIFICATION_FAILED"));
  }

  @ExceptionHandler(com.eliteshop.colombia.customer.domain.exception.AvatarNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleAvatarNotFound(
      com.eliteshop.colombia.customer.domain.exception.AvatarNotFoundException ex) {
    log.warn("Avatar no encontrado: {}", ex.getMessage());
    return notFound("Avatar no encontrado", "AVATAR_NOT_FOUND");
  }

  @ExceptionHandler(
      com.eliteshop.colombia.review.domain.exception.ReviewNotPurchasedException.class)
  public ResponseEntity<ErrorResponse> handleReviewNotPurchased(
      com.eliteshop.colombia.review.domain.exception.ReviewNotPurchasedException ex) {
    log.warn("Review no permitida: {}", ex.getMessage());
    return forbidden("Solo puedes reseñar productos que hayas comprado", "REVIEW_NOT_PURCHASED");
  }

  @ExceptionHandler(
      com.eliteshop.colombia.seller.domain.exception.SellerVerificationException.class)
  public ResponseEntity<ErrorResponse> handleSellerVerification(
      com.eliteshop.colombia.seller.domain.exception.SellerVerificationException ex) {
    log.warn("Verificacion de vendedor fallida: {}", ex.getMessage());
    return badRequest("Verificacion de vendedor fallida", "SELLER_VERIFICATION_FAILED");
  }

  @ExceptionHandler(
      com.eliteshop.colombia.product.domain.exception.StockInsufficientException.class)
  public ResponseEntity<ErrorResponse> handleStockInsufficient(
      com.eliteshop.colombia.product.domain.exception.StockInsufficientException ex) {
    log.warn("Stock insuficiente: {}", ex.getMessage());
    return conflict("Stock insuficiente", "STOCK_INSUFFICIENT");
  }

  @ExceptionHandler(
      com.eliteshop.colombia.cart.domain.exception.CartItemAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleCartItemAlreadyExists(
      com.eliteshop.colombia.cart.domain.exception.CartItemAlreadyExistsException ex) {
    log.warn("Item ya existe en carrito: {}", ex.getMessage());
    return conflict("El producto ya esta en el carrito", "CART_ITEM_ALREADY_EXISTS");
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
    log.warn("Estado ilegal: {}", ex.getMessage());
    return badRequest("Operacion no permitida en el estado actual", "ILLEGAL_STATE");
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
    log.warn("Argumento ilegal: {}", ex.getMessage());
    return badRequest(ex.getMessage(), "ILLEGAL_ARGUMENT");
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
    log.error("Error inesperado: {}", ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno del servidor",
                "INTERNAL_ERROR"));
  }

  private ResponseEntity<ErrorResponse> unauthorized(String message, String code) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ErrorResponse.of(HttpStatus.UNAUTHORIZED.value(), message, code));
  }

  private ResponseEntity<ErrorResponse> notFound(String message, String code) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ErrorResponse.of(HttpStatus.NOT_FOUND.value(), message, code));
  }

  private ResponseEntity<ErrorResponse> conflict(String message, String code) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ErrorResponse.of(HttpStatus.CONFLICT.value(), message, code));
  }

  private ResponseEntity<ErrorResponse> badRequest(String message, String code) {
    return ResponseEntity.badRequest()
        .body(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), message, code));
  }

  private ResponseEntity<ErrorResponse> forbidden(String message, String code) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(ErrorResponse.of(HttpStatus.FORBIDDEN.value(), message, code));
  }
}
