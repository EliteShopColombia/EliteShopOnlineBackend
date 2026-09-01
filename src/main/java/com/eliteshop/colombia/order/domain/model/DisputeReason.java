package com.eliteshop.colombia.order.domain.model;

public enum DisputeReason {
  PRODUCT_DAMAGED(false),
  PRODUCT_DEFECTIVE(false),
  WRONG_ITEM(true),
  NOT_AS_DESCRIBED(true),
  QUALITY_NOT_SATISFACTORY(true),
  LATE_DELIVERY(true),
  NO_LONGER_NEEDED(true),
  OTHER(false);

  private final boolean restockable;

  DisputeReason(boolean restockable) {
    this.restockable = restockable;
  }

  public boolean isRestockable() {
    return restockable;
  }
}
