package com.eliteshop.colombia.shared.notification.domain.exception;

public class NotificationFailedException extends RuntimeException {

  public NotificationFailedException(String message, Throwable cause) {
    super(message, cause);
  }
}
