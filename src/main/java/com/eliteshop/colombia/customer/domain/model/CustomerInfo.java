package com.eliteshop.colombia.customer.domain.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class CustomerInfo {
  @NonNull private final CustomerDniType dniType;
  @NonNull private final CustomerDniNumber dniNumber;
  @NonNull private final CustomerAddress address;
  @NonNull private final CustomerDepartment department;
  @NonNull private final CustomerCity city;
  @NonNull private final CustomerDniCreatedAt dniCreatedAt;
  private final CustomerDniUpdatedAt dniUpdatedAt;
}
