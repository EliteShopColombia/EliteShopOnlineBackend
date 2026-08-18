package com.eliteshop.colombia.order.domain.model;

import java.sql.Timestamp;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class OrderCreatedAt {
  private final Timestamp value;
}
