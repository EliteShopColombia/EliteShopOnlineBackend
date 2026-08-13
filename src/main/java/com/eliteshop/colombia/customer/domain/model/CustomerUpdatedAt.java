package com.eliteshop.colombia.customer.domain.model;

import java.sql.Timestamp;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class CustomerUpdatedAt {
  private final Timestamp value;
}
