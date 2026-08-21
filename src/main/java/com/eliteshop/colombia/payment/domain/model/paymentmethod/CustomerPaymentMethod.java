package com.eliteshop.colombia.payment.domain.model.paymentmethod;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class CustomerPaymentMethod {
  @NonNull private final CustomerPaymentMethodId id;
  @NonNull private final CustomerPaymentMethodCustomerId customerId;
  @NonNull private final CustomerPaymentMethodEpaycoToken epaycoToken;
  @NonNull private final CustomerPaymentMethodEpaycoCustomerId epaycoCustomerId;
  @NonNull private final CustomerPaymentMethodLast4 last4;
  @NonNull private final CustomerPaymentMethodBrand brand;
  @NonNull private final CustomerPaymentMethodExpiryMonth expiryMonth;
  @NonNull private final CustomerPaymentMethodExpiryYear expiryYear;
  private final CustomerPaymentMethodDocType docType;
  private final CustomerPaymentMethodDocNumber docNumber;
  private final boolean isDefault;
  @NonNull private final LocalDateTime createdAt;

  public static CustomerPaymentMethod create(
      CustomerPaymentMethodCustomerId customerId,
      CustomerPaymentMethodEpaycoToken token,
      CustomerPaymentMethodEpaycoCustomerId epaycoCustomerId,
      CustomerPaymentMethodLast4 last4,
      CustomerPaymentMethodBrand brand,
      CustomerPaymentMethodExpiryMonth expiryMonth,
      CustomerPaymentMethodExpiryYear expiryYear,
      CustomerPaymentMethodDocType docType,
      CustomerPaymentMethodDocNumber docNumber,
      boolean isDefault) {
    return new CustomerPaymentMethod(
        CustomerPaymentMethodId.generate(),
        customerId,
        token,
        epaycoCustomerId,
        last4,
        brand,
        expiryMonth,
        expiryYear,
        docType,
        docNumber,
        isDefault,
        LocalDateTime.now());
  }

  public CustomerPaymentMethod asDefault() {
    return new CustomerPaymentMethod(
        this.id,
        this.customerId,
        this.epaycoToken,
        this.epaycoCustomerId,
        this.last4,
        this.brand,
        this.expiryMonth,
        this.expiryYear,
        this.docType,
        this.docNumber,
        true,
        this.createdAt);
  }
}
