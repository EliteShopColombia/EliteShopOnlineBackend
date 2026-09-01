package com.eliteshop.colombia.order.infrastructure.controller.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchOrdersResponse {

  private List<OrderResponse> orders;
  private int totalElements;
  private int page;
  private int size;
  private int totalPages;
}
