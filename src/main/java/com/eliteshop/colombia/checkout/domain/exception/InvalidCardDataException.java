package com.eliteshop.colombia.checkout.domain.exception;

/**
 * Los datos de la tarjeta son inválidos o están incompletos.
 *
 * <p>Antes esta situación reutilizaba {@link InsufficientStockException}, de modo que el cliente
 * recibía un 409 "Stock insuficiente" cuando el problema real era la tarjeta. Una excepción
 * prestada de otro dominio envía al usuario a mirar donde no está el fallo.
 */
public class InvalidCardDataException extends RuntimeException {

  public static final String CODE = "INVALID_CARD_DATA";

  public InvalidCardDataException(String message) {
    super(message);
  }
}
