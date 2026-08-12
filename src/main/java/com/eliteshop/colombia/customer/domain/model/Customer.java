package com.eliteshop.colombia.customer.domain.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class Customer {
  @NonNull private final CustomerId id;
  @NonNull private final CustomerFirstName firstName;
  @NonNull private final CustomerLastName lastName;
  @NonNull private final CustomerEmail email;
  @NonNull private final CustomerPhoneNumber phoneNumber;
  @NonNull private final CustomerPassword password;
  private final CustomerProfileImage profileImage;
  @NonNull private final CustomerCreatedAt createdAt;
  private final CustomerUpdatedAt updatedAt;
  private final CustomerInfo info;
}
