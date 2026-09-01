package com.eliteshop.colombia.seller.domain.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class SellerProfileImage {
  @NonNull private final String value;
}
